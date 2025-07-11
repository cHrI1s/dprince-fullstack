package com.dprince.apis.users;

import com.dprince.apis.users.models.LoginRequest;
import com.dprince.apis.users.models.OtpResendRequest;
import com.dprince.apis.users.models.PasswordRequest;
import com.dprince.apis.users.models.UserCreator;
import com.dprince.apis.users.models.validation.UserCreationValidation;
import com.dprince.apis.users.models.validation.UserPasswordUpdate;
import com.dprince.apis.users.models.validation.UserUpdateValidation;
import com.dprince.apis.users.validations.EmailOTPLogin;
import com.dprince.apis.users.validations.OTPLogin;
import com.dprince.apis.users.validations.UsernameLogin;
import com.dprince.apis.users.vos.OtpLog;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.AppStringUtils;
import com.dprince.apis.utils.NetworkUtils;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.configuration.ApplicationConfig;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.sms.SmsService;
import com.dprince.configuration.sms.models.SmsSendRequest;
import com.dprince.entities.*;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.OTPType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.PageVO;
import com.dprince.scheduler.task.BackupGenerator;
import com.dprince.security.UpdatableBCrypt;
import com.dprince.security.user.UserSession;
import com.dprince.startup.GeneralValues;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {
    private final AppRepository repository;
    private final UserSession userSession;
    private final EmailService emailService;
    private final SmsService smsService;
    private final BackupGenerator backupGenerator;



    @PostMapping("/login")
    public UserSession login(@Validated({UsernameLogin.class}) @RequestBody LoginRequest loginRequest,
                             @NonNull HttpServletRequest request)
            throws LoginException, BadRequestException {
        String loginMessage = "Login Attempt",
                content = loginRequest.getUsername();
        NotificationActionType actionType = NotificationActionType.DELETE;
        UserSession session = loginRequest.getPassword()!=null
                ? userSession.login(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.isRememberUser())
                : userSession.login(loginRequest.getUsername());
        if(session.getUser()==null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Login Failed.");

        /*
         * Nimba umu user atari umwe muba super admins, then raba ko institution yiwe itari blocked
         */
        Institution institution = null;
        if(!User.noOrganizationUsers().contains(session.getUser().getUserType())){
            institution = this.repository.getInstitutionRepository()
                    .findById(session.getUser().getInstitutionId())
                    .orElse(null);
            if(institution==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Institution does not exist.");
            }

            String requestIp = NetworkUtils.getUserIp(request);
            if(!StringUtils.isEmpty(institution.getIpAddress())){
                if(!institution.getIpAddress().equalsIgnoreCase(requestIp)){
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "You are not allowed to perform this operation when not in your institution premises.");
                }
            }
            String expirationMessage = institution.getExpirationMessage(true);
            if(!StringUtils.isEmpty(expirationMessage)){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        expirationMessage);
            }

            SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS.get(institution.getSubscriptionPlan());
            if(subscriptionPlan==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Failed to load your Institution's data");
            }
            institution.setMaxChurchBranches(subscriptionPlan.getChurchBranches());
            institution.setMaxSmes(subscriptionPlan.getSmses());
            institution.setMaxEmails(subscriptionPlan.getEmails());
            institution.setMaxWhatsapp(subscriptionPlan.getWhatsapp());
            institution.setMaxMembers(subscriptionPlan.getMembers());
            institution.setMaxAdmins(subscriptionPlan.getAdmins());
            institution.setAllowedFeatures(subscriptionPlan.getFeatures());

            institution.populateCertificates(this.repository);

            if(institution.isBlocked()){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Your institution has been blocked. Please do contact the administrator.");
            }
            session.getUser().setInstitution(institution);

            actionType = NotificationActionType.READ;
        }

        Notification notification = Notification.createNotification(
                session.getUser(),
                loginMessage,
                content,
                actionType,
                (institution==null) ? null : institution.getId()
        );
        this.repository.getNotificationsRepository().save(notification);
        return session;
    }



    @PostMapping("/email-login")
    public ApiWorkFeedback emailLogin(@Validated({EmailOTPLogin.class}) @RequestBody LoginRequest loginRequest)
            throws BadRequestException {
        String loginMessage = "Login Attempt : Email";
        NotificationActionType actionType = NotificationActionType.DELETE;
        User user = this.repository.getUsersRepository().findByUsername(loginRequest.getEmail());
        if(user==null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
        String content = user.getUsername()+": Attempt to login via SMS OTP.";

        if(!User.noOrganizationUsers().contains(user.getUserType())) {
            Institution institution = this.repository.getInstitutionRepository()
                    .findById(user.getInstitutionId())
                    .orElseThrow(() -> {
                        // Throw an error
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "We could not load your institution details.");
                    });
            if (!institution.canCommunicate(this.repository, CommunicationWay.SMS)) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Your institution quota has been reached. Please contact your administrator.");
            }
        }

        OTP otp = new OTP();
        otp.setUserId(user.getId());
        otp.setType(OTPType.LOGIN);
        otp.generateOTP();
        this.repository.getOtpRepository().save(otp);

        String sms = otp.getLoginOTPMessage();
        boolean otpSent = emailService.applicationSendHTMLEmail(
                loginRequest.getEmail(),
                ApplicationConfig.APPLICATION_NAME + ": Login via OTP",
                null,
                sms
        );
        OtpLog log = OtpLog.builder().username(user.getUsername()).build();

        Notification loginNotification = Notification.createNotification(
                user,
                loginMessage,
                content,
                actionType,
                user.getInstitutionId()
        );
        CompletableFuture.runAsync(()->this.repository.getNotificationsRepository().save(loginNotification));
        return ApiWorkFeedback.builder()
                .object(otpSent ? log : null)
                .successful(otpSent)
                .message(otpSent
                        ? "An OTP number has been sent to your Email. Please check your inbox and type it in below."
                        : "Failed to sent OTP.")
                .build();
    }


    @PostMapping("/phone-login")
    public ApiWorkFeedback phoneLogin(@Validated({OTPLogin.class}) @RequestBody LoginRequest loginRequest)
            throws BadRequestException {
        String loginMessage = "Login Attempt : SMS";
        NotificationActionType actionType = NotificationActionType.DELETE;
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<User> usersPage = this.repository.getUsersRepository()
                .findByPhone(loginRequest.getPhone(), pageRequest);
        if(usersPage.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
        }
        User user = usersPage.getContent().get(0);
        String content = user.getUsername()+": Attempt to login via SMS OTP.";

        if(user.getInstitutionId()!=null) {
            Institution institution = this.repository.getInstitutionRepository()
                    .findById(user.getInstitutionId())
                    .orElseThrow(() -> {
                        // Throw an error
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "We could not load your institution details.");
                    });
            if (!institution.canCommunicate(this.repository, CommunicationWay.SMS)) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Your institution SMS quota has been reached. Please contact your administrator.");
            }
        }

        OTP otp = new OTP();
        otp.setUserId(user.getId());
        otp.setType(OTPType.LOGIN);
        otp.generateOTP();
        this.repository.getOtpRepository().save(otp);

        String sms = otp.getLoginOTPMessage();
        SmsSendRequest smsSendRequest = SmsSendRequest.builder()
                .message(sms)
                .numbers(Collections.singletonList(user.getPhone().toString()))
                .institutionId(user.getInstitutionId())
                .build();
        boolean otpSent = this.smsService.sendMessage(smsSendRequest);

        OtpLog log = OtpLog.builder().username(user.getUsername()).build();

        CompletableFuture.runAsync(()->{
            Notification loginNotification = Notification.createNotification(
                    user,
                    loginMessage,
                    content,
                    actionType,
                    user.getInstitutionId()
            );
            this.repository.getNotificationsRepository().save(loginNotification);
        });
        return ApiWorkFeedback.builder()
                .object(otpSent ? log : null)
                .successful(otpSent)
                .message(otpSent
                        ? "An OTP number has been sent to your Phone number. Please check your smses and type it in below."
                        : "Failed to sent OTP.")
                .build();
    }

    @PostMapping("/resend-otp")
    public ApiWorkFeedback resendOTP(@RequestBody OtpResendRequest request){
        if(StringUtils.isEmpty(request.getEmail()) && request.getPhone()==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Phone or Email not provided!");
        }

        LoginRequest loginRequest = new LoginRequest();
        switch(request.getOtpType()){
            case MAIL:
                loginRequest.setEmail(request.getEmail());
                return this.emailLogin(loginRequest);

            case SMS:
                loginRequest.setPhone(request.getPhone());
                return this.phoneLogin(loginRequest);
        }
        // Thrown an error
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not recognized.");
    }


    @PostMapping("/login-verify-otp")
    public UserSession verifyOTP(@Valid @RequestBody LoginRequest loginRequest,
                                 @NonNull HttpServletRequest request)
            throws BadRequestException, LoginException {
        User user = this.repository.getUsersRepository()
                .findByUsername(loginRequest.getUsername());
        if(user==null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not recognized.");
        }
        OTP otp = this.repository.getOtpRepository()
                .findByOtpAndUserId(loginRequest.getOtp(), user.getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong OTP provided"));

        otp.verifyOTP();
        UserSession session = this.login(loginRequest, request);
        this.repository.getOtpRepository()
                .deleteById(otp.getId());
        return session;
    }



    @Authenticated
    @GetMapping("/logout")
    public ApiWorkFeedback logout(@RequestHeader HttpHeaders headers,
                              @NonNull HttpSession session)
            throws BadRequestException {
        User loggedInUser = User.getUserFromSession(session);
        Institution institution = null;
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            institution = this.repository.getInstitutionRepository()
                    .findById(loggedInUser.getInstitutionId())
                    .orElse(null);
        }

        String fullName = loggedInUser.getFirstName()+" "+loggedInUser.getLastName();
        String token = (headers != null && headers.get("authorization") != null)
                ? Objects.requireNonNull(headers.get("authorization"))
                    .get(0)
                    .substring(7)
                : null;
        this.userSession.logout(loggedInUser, token);

        Notification notification = Notification.createNotification(
                loggedInUser,
                "Logout",
                fullName,
                NotificationActionType.UPDATE,
                (institution==null) ? null : institution.getId()
        );
        this.repository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Logout Successful")
                .successful(true)
                .build();
    }



    @Authenticated
    @PostMapping("/update-user")
    public User updateUser(@RequestBody @Validated({UserUpdateValidation.class}) UserCreator userCreator,
                           @NonNull HttpSession session){
        return this.createUser(userCreator, session);
    }



    @Authenticated
    @PostMapping("/update-user-password")
    public User updateUserPassword(@RequestBody @Validated({UserPasswordUpdate.class}) UserCreator userCreator,
                           @NonNull HttpSession session){
        userCreator.setPasswordUpdate(true);
        return this.createUser(userCreator, session);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    //
    @PostMapping("/create")
    public User createUser(@RequestBody @Validated({UserCreationValidation.class}) UserCreator userCreator,
                           @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        List<UserType> allowedToCreateUserTypes = loggedInUser.getAllowedToCreateUserTypes();
        if(!allowedToCreateUserTypes.contains(userCreator.getUserType())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You are not allowed to create this user.");
        }
        User user = null;
        boolean update = false,
                passwordUpdate = false;
        if(userCreator.getId()!=null) {
            user = this.repository.getUsersRepository()
                    .findById(userCreator.getId())
                    .orElse(null);
            if(user==null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "The User does not exist.");
            }
            update = true;
            passwordUpdate = userCreator.isPasswordUpdate();
        } else {
            user = this.repository.getUsersRepository()
                    .findByUsername(userCreator.getUsername());
            if(user!=null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "This email is used by someone else.");
            }
            user = this.repository.getUsersRepository()
                    .findByStaffId(userCreator.getStaffId());
            if(user!=null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Staff Id is taken.");
            }
            if(!userCreator.getPassword().equals(userCreator.getConfirmPassword())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Passwords do not match.");
            }
            user = new User();
        }

        if(!passwordUpdate) {
            // Update this when this is not a password update
            user.setFirstName(userCreator.getFirstName());
            user.setLastName(userCreator.getLastName());
            user.setGender(userCreator.getGender());
            user.setUserType(userCreator.getUserType());
            user.setPhone(userCreator.getPhone());
            user.setStaffId(userCreator.getStaffId());
        }
        if(!update || passwordUpdate) {
            if(!passwordUpdate){
                user.setUsername(userCreator.getUsername());
                user.setEmail(userCreator.getUsername());
            }
            String password = UpdatableBCrypt.hash(userCreator.getPassword());
            user.setPassword(password);
        }

        // Add users to your current organization
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            // this means we are creating a super admin or sub super admin
            user.setInstitutionId(loggedInUser.getInstitutionId());
        }
        user = this.repository.getUsersRepository().save(user);

        String notificationTitle = update ? "User Updated." : "User created.";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                user.getStaffId(),
                NotificationActionType.UPDATE,
                user.getInstitutionId()
        );
        this.repository.getNotificationsRepository().save(notification);
        return user;
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{userId}")
    public ApiWorkFeedback deleteUser(@PathVariable("userId") Long userId,
                                      @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        User userToDelete = this.repository.getUsersRepository()
                .findById(userId)
                .orElse(null);
        if(userToDelete==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The user you are trying to delete does not exist in our database.");
        }

        if(userToDelete.getId().equals(loggedInUser.getId())){
            // Buza umuntukwifuta.
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You can not delete your own account.");
        }

        boolean canDelete = User.noOrganizationUsers().contains(loggedInUser.getUserType());
        if(!canDelete){
            // This means the user is not among super admin
            canDelete = userToDelete.getInstitutionId()
                    .equals(loggedInUser.getInstitutionId());
        }

        String message = "Could not delete the User.";
        boolean done = false;
        if(canDelete){
            this.repository.getUsersRepository().deleteById(userId);
            done = true;
            message = "User deleted!.";

            String notificationTitle = "User Deleted.";
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    notificationTitle,
                    userToDelete.getStaffId(),
                    NotificationActionType.UPDATE,
                    userToDelete.getInstitutionId()
            );
            this.repository.getNotificationsRepository().save(notification);
        }
        return ApiWorkFeedback.builder().message(message).successful(done).build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/list")
    public PageVO listUsers(@RequestBody @Valid ListQuery listQuery,
                            @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        List<UserType> allowedToCreate = loggedInUser.getAllowedToCreateUserTypes();
        int page = listQuery.getPage()-1;

        PageRequest pageable = PageRequest.of(page,
                listQuery.getSize(),
                Sort.by(Sort.Direction.ASC, User.Fields.firstName, User.Fields.lastName));

        Page<User> users = null;
        if(loggedInUser.getUserType().equals(UserType.SUPER_ADMINISTRATOR)){
            // this means we are looking for users that do not need organizationId;
            if(AppStringUtils.isEmpty(listQuery.getQuery())){
                users = this.repository.getUsersRepository()
                        .findAllByUserTypeIn(allowedToCreate, pageable);
            } else {
                users = this.repository.getUsersRepository()
                        .findSimilar(allowedToCreate,
                                listQuery.getQuery().toLowerCase(),
                                pageable);
            }
        }
        if(users==null) return new PageVO();
        return PageVO.getPageVO(users);
    }


    @Authenticated
    @PostMapping("/update-my-password")
    public ApiWorkFeedback updateMyPassword(@RequestBody @Valid PasswordRequest passwordRequest,
                                            @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Passwords do not match.");
        }
        String password = UpdatableBCrypt.hash(passwordRequest.getPassword());
        loggedInUser.setPassword(password);
        this.repository.getUsersRepository().save(loggedInUser);
        return ApiWorkFeedback.builder()
                .message("Password changed.")
                .successful(true)
                .build();
    }

//
    @GetMapping("/backup")
    public ApiWorkFeedback backup(){
        this.backupGenerator.backup(null, null);
        return ApiWorkFeedback.builder()
                .message("Backup")
                .successful(true)
                .build();
    }
}




