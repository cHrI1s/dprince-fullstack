package com.dprince.apis.institution;

import com.dprince.apis.institution.models.*;
import com.dprince.apis.institution.models.donations.DonationContribution;
import com.dprince.apis.institution.models.files.CertificateImporter;
import com.dprince.apis.institution.models.files.DonationsImporter;
import com.dprince.apis.institution.models.files.FamiliesImporter;
import com.dprince.apis.institution.models.files.MemberImporter;
import com.dprince.apis.institution.models.parts.MemberAction;
import com.dprince.apis.institution.models.parts.ProspectusMember;
import com.dprince.apis.institution.models.validations.*;
import com.dprince.apis.institution.validators.GeneralDonationValidation;
import com.dprince.apis.institution.vos.PartnerDonationVO;
import com.dprince.apis.institution.vos.Receiptor;
import com.dprince.apis.misc.receipt.model.ReceiptListModel;
import com.dprince.apis.misc.receipt.model.ReceiptModel;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.AppStringUtils;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.Messenger;
import com.dprince.apis.utils.models.MultiData;
import com.dprince.apis.utils.models.SimpleGetModel;
import com.dprince.apis.utils.vos.InstitutionSimpleVO;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.configuration.sms.SmsService;
import com.dprince.configuration.sms.models.SmsSendRequest;
import com.dprince.configuration.whatsapp.WhatsappService;
import com.dprince.configuration.whatsapp.utils.WhatsappMessageConfig;
import com.dprince.entities.*;
import com.dprince.entities.enums.*;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.PageVO;
import com.dprince.security.UpdatableBCrypt;
import com.dprince.startup.GeneralValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
//
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/institution")
public class InstitutionController {
    private final EmailService emailService;

    private final EntityManagerFactory entityManagerFactory;
    private EntityManager getEntityManager(){
        return this.entityManagerFactory.createEntityManager();
    }

    private final AppRepository appRepository;
    private final LocalFileStorageService storageService;
    private final SmsService smsService;
    private final WhatsappService whatsappService;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionController.class); // <-- Inga 'InstitutionController.class' nu irukkanum


    public static List<String> omittedLinks = Arrays.asList();


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-organization")
    public ApiWorkFeedback saveOrganization(@RequestBody @Validated({OrganizationCreationValidator.class})
                                            Institution institution,
                                            @NonNull HttpSession session){
        institution.setInstitutionType(InstitutionType.GENERAL);
        return this.saveChurch(institution, session);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR
    })
    @PostMapping("/save-church-branch")
    public ApiWorkFeedback saveChurchBranch(@RequestBody @Validated({BranchChurchCreationValidator.class, })
                                            Institution institution,
                                            @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(institution.getParentInstitutionId()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "HeadQuarter Church not specified.");
            }
        } else {
            institution.setParentInstitutionId(loggedInUser.getInstitutionId());
        }
        institution.setInstitutionType(InstitutionType.CHURCH);
        Institution parentInstitution = this.appRepository
                .getInstitutionRepository()
                .findById(institution.getParentInstitutionId())
                .orElseThrow(()->{
                    // Send back an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Main branch not recognized.");
                });
        SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS.get(parentInstitution.getSubscriptionPlan());
        if(subscriptionPlan==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to load Main branch data.");
        }
        if(!parentInstitution.canCreateBranch(subscriptionPlan)){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Can not create new branch. Quota reached.");
        }
        institution.setSubscription(parentInstitution.getSubscription());
        institution.setDeadline(parentInstitution.getDeadline());
        institution.setMaxSmes(parentInstitution.getMaxSmes());
        institution.setMaxWhatsapp(parentInstitution.getMaxWhatsapp());
        institution.setEmails(parentInstitution.getEmails());
        institution.setChurchType(ChurchType.BRANCH);
        institution.setParentInstitution(parentInstitution);
        institution.setCategory(parentInstitution.getCategory());
        institution.setSubscriptionPlan(parentInstitution.getSubscriptionPlan());
        institution.setBaseCode(parentInstitution.getBaseCode()+"-"+institution.getBaseCode());
        return this.saveChurch(institution, session);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update-institution")
    public ApiWorkFeedback updateInstitution(@RequestBody @Validated({InstitutionUpdateValidator.class})
                                             Institution institution,
                                             @NonNull HttpSession session){
        return this.saveChurch(institution, session);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update-branch")
    public ApiWorkFeedback updateBranch(@RequestBody @Validated({BranchUpdateValidator.class})
                                        Institution institution,
                                        @NonNull HttpSession session){
        return this.saveChurchBranch(institution, session);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-church")
    public ApiWorkFeedback saveChurch(@RequestBody @Validated({ChurchCreationValidator.class})
                                      Institution institution,
                                      @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        boolean isUpdate = institution.getId()!=null;
        if(isUpdate){
            Institution savedInstitution =
                    this.appRepository.getInstitutionRepository()
                            .findById(institution.getId()).orElse(null);
            if(savedInstitution==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "The institution you are trying to update does not exist.");
            }
            institution.setId(savedInstitution.getId());
            if(AppStringUtils.isEmpty(institution.getLogo())) {
                institution.setLogo(savedInstitution.getLogo());
            }
            institution.setPhone(savedInstitution.getPhone());
            institution.setBaseCode(savedInstitution.getBaseCode());
            institution.setEmail(savedInstitution.getEmail());
            institution.setCreationDate(savedInstitution.getCreationDate());
            institution.setId(savedInstitution.getId());
        } else {
            institution.computeSubscriptionDeadline();
            institution.adjustSubscriptionPlan(this.appRepository);
            Optional<Institution> optionalInstitution =
                    this.appRepository.getInstitutionRepository()
                            .findByEmail(institution.getEmail());
            User optionalUser = this.appRepository
                    .getUsersRepository()
                    .findByUsername(institution.getEmail());
            if (optionalInstitution.isPresent() || optionalUser!=null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "The email is already in use.");
            }

            optionalInstitution = this.appRepository.getInstitutionRepository()
                    .findByBaseCode(institution.getBaseCode());
            if (optionalInstitution.isPresent()) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Base code not available.");
            }

            this.appRepository.getInstitutionRepository()
                    .findByPhone(institution.getPhone())
                    .ifPresent(phoneInstitution->{
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "There is already an institution with such Phone number.");
                    });
        }
        Institution parentInstitution = institution.getParentInstitution(),
                savedInstitution;
        try {
            savedInstitution = this.appRepository.getInstitutionRepository()
                    .save(institution);
        } catch(Exception exception){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Something wrong happened during the creation of the Institution.");
        }

        String notificationTitle = (isUpdate ? "Updated " : "Created ")
                +savedInstitution.getInstitutionTypeName();
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedInstitution.getName(),
                isUpdate ? NotificationActionType.UPDATE : NotificationActionType.CREATE,
                savedInstitution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        if(!isUpdate){
            // This creates a new user
            User user = User.generateUser(savedInstitution);
            this.appRepository.getUsersRepository().save(user);

            CompletableFuture.runAsync(()->{
                if(parentInstitution!=null){
                    // Update the number of branches;
                    parentInstitution.updateChurchBranches(true);
                    this.appRepository.getInstitutionRepository().save(parentInstitution);
                }
                SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS
                        .get(savedInstitution.getSubscriptionPlan());
                if(subscriptionPlan!=null) {
                    savedInstitution.setInstitutionPlan(subscriptionPlan);
                    emailService.sendSimpleEmail(user.getEmail(),
                            "Welcome to DNote",
                            user.getWelcomeMessage(savedInstitution));
                }
            });
        }

        savedInstitution.attachSubscriptionPlan(this.appRepository);
        GeneralValues.addInstitution(savedInstitution);
        String institutionName = savedInstitution.getInstitutionTypeName();
        String message = (isUpdate)
                ? institutionName+" updated."
                : institutionName+" saved.";
        return ApiWorkFeedback.builder()
                .message(message)
                .object(savedInstitution)
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/multiple-toggle-block")
    public ApiWorkFeedback toggleMultipleBlock(@RequestBody @Valid MultiData data,
                                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        List<Institution> savedInstitutions = this.appRepository.getInstitutionRepository()
                .findAllById(data.getItemsIds());

        savedInstitutions.parallelStream().forEach(item->
                this.toggleBlockInstitution(item, loggedInUser));

        return ApiWorkFeedback.builder()
                .message("Institutions blocked/unblocked")
                .successful(true)
                .build();
    }



    private void toggleBlockInstitution(Institution savedInstitution,
                                        User loggedInUser){
        boolean currentBlockStatus = savedInstitution.isBlocked();
        savedInstitution.setBlocked(!currentBlockStatus);
        savedInstitution = this.appRepository.getInstitutionRepository().save(savedInstitution);
        String notificationTitle = (currentBlockStatus ? "Blocked " : "Unblocked ")
                + savedInstitution.getInstitutionTypeName();
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedInstitution.getName(),
                NotificationActionType.UPDATE,
                savedInstitution.getId()
        );
        GeneralValues.addInstitution(savedInstitution);
        this.appRepository.getNotificationsRepository().save(notification);
    }
    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/toggle-block")
    public ApiWorkFeedback getAllInstitution(@RequestBody @Valid InstitutionBlockerModel model,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Institution savedInStitution = this.appRepository.getInstitutionRepository()
                .findById(model.getInstitutionId())
                .orElse(null);
        if(savedInStitution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution does not exist.");
        }
        boolean currentBlockStatus = savedInStitution.isBlocked();
        this.toggleBlockInstitution(savedInStitution, loggedInUser);
        return ApiWorkFeedback.builder()
                .message(savedInStitution.getInstitutionTypeName()
                        + (currentBlockStatus
                        ? " Unblocked"
                        : " Blocked"
                ))
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get")
    public PageVO getAllInstitution(@RequestBody @Valid InstitutionListing listQuery,
                                    @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            loggedInUser.checkUserFulfillsRequirements(listQuery, this.appRepository);
        } else {
            listQuery.setInstitutionId(loggedInUser.getInstitutionId());
        }
        listQuery.setLoggedInUser(loggedInUser);


        Map<Long, SubscriptionPlan> subscriptionPlansMap = GeneralValues.SUBSCRIPTIONS;
        Page<Institution> page = Institution.makeSearch(this.getEntityManager(),
                this.appRepository,
                listQuery);
        page.getContent()
                .parallelStream()
                .forEach(institution->{
                    List<AppFeature> features = subscriptionPlansMap
                            .get(institution.getSubscriptionPlan())
                            .getFeatures();
                    institution.setAllowedFeatures(features);

                    Map<CertificateType, String> certificates = this.appRepository
                            .getCertificatesBackgroundsRepository()
                            .findAllByInstitutionId(institution.getId())
                            .parallelStream()
                            .collect(Collectors.toMap(CertificateBackground::getCertificateType,
                                    CertificateBackground::getFileName));
                    institution.setCertificates(certificates);
                });
        return PageVO.getPageVO(page);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/renew-subscription")
    public ApiWorkFeedback renewInstitutionSubscription(@RequestBody @Valid SubscriptionRenewal renewal,
                                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(renewal.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution does not exist.");
        }

        if(institution.getSubscription().equals(Subscription.LIFETIME)){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You can not renew an institution that has a lifetime subscription");
        }
        SubscriptionPlan nextPlan = GeneralValues.SUBSCRIPTIONS.get(renewal.getPlanId());
        if(nextPlan==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Subscription Plan not recognized.");
        }
        Date oldDeadline = institution.getDeadline(),
                newDeadline = institution.computeDeadline(renewal.getSubscription());
        institution.setDeadline(newDeadline);
        institution.setSubscription(renewal.getSubscription());
        Institution savedInstitution = this.appRepository.getInstitutionRepository().save(institution);
        savedInstitution.populate(appRepository);
        CompletableFuture.runAsync(()->{
            // Get all the branches of this institution
            appRepository.getInstitutionRepository()
                    .findAllByParentInstitutionId(institution.getId())
                    .parallelStream()
                    .forEach(singleBranch->{
                        singleBranch.setSubscriptionPlan(nextPlan.getId());
                        this.appRepository.getInstitutionRepository().save(singleBranch);
                    });

            String notificationTitle = "Subscription Renewal: "+institution.getName();
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    notificationTitle,
                    nextPlan.getName(),
                    NotificationActionType.UPDATE,
                    institution.getId()
            );
            this.appRepository.getNotificationsRepository().save(notification);


            // Save the renewal so that when the date arrives we should change the name of the Subscription
            InstitutionSubscriptionRenewal institutionRenewal = new InstitutionSubscriptionRenewal();
            institutionRenewal.setInstitutionId(savedInstitution.getId());
            institutionRenewal.setNewDeadline(newDeadline);
            institutionRenewal.setSubscription(renewal.getSubscription());
            institutionRenewal.setSubscriptionPlanId(nextPlan.getId());
            institutionRenewal.setOldDeadline(oldDeadline);
            this.appRepository.getRenewalsRepository().save(institutionRenewal);

        });
        GeneralValues.addInstitution(savedInstitution);
        return ApiWorkFeedback.builder()
                .message("Subscription Renewed.")
                .object(savedInstitution)
                .successful(true)
                .build();
    }




    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/upgrade-plan")
    public ApiWorkFeedback upgradeInstitutionPlan(@RequestBody @Valid PlanUpgrader upgrader,
                                                  @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(upgrader.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution does not exist.");
        }

        SubscriptionPlan nextPlan = GeneralValues.SUBSCRIPTIONS.get(upgrader.getPlanId());
        if(nextPlan==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Subscription Plan not recognized.");
        }
        institution.setSubscriptionPlan(nextPlan.getId());
        institution.setSmses(0L);
        institution.setWhatsapp(0L);
        institution.setEmails(0L);
        Institution savedInstitution = this.appRepository.getInstitutionRepository().save(institution);
        savedInstitution.populate(appRepository);
        CompletableFuture.runAsync(()->{
            // Get all the branches of this institution
            appRepository.getInstitutionRepository()
                    .findAllByParentInstitutionId(institution.getId())
                    .parallelStream()
                    .forEach(singleBranch->{
                        singleBranch.setSubscriptionPlan(nextPlan.getId());
                        this.appRepository.getInstitutionRepository().save(singleBranch);
                    });

            String notificationTitle = "Plan Change to "+nextPlan.getName()+": "+institution.getName();
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    notificationTitle,
                    nextPlan.getName(),
                    NotificationActionType.UPDATE,
                    institution.getId()
            );
            this.appRepository.getNotificationsRepository().save(notification);
        });
        GeneralValues.addInstitution(savedInstitution);
        return ApiWorkFeedback.builder()
                .message("Plan Changed.")
                .object(savedInstitution)
                .successful(true)
                .build();
    }



    private void deleteLogo(Institution institution, LocalFileStorageService storageService){
        if(!AppStringUtils.isEmpty(institution.getLogo())){
            CompletableFuture.runAsync(()-> storageService.delete(institution.getLogo(), ApplicationFileType.FILE) );
        }
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/multi-delete")
    public ApiWorkFeedback deleteMultiInstitution(@RequestBody @Valid MultiData data,
                                                  @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        List<Institution> institutions = this.appRepository.getInstitutionRepository()
                .findAllById(data.getItemsIds());

        CompletableFuture.runAsync(()->{
            List<Notification> notifications = new ArrayList<>();
            institutions.parallelStream().forEach(item->{
                String notificationTitle = "Deleted "+item.getInstitutionTypeName();
                Notification notification = Notification.createNotification(
                        loggedInUser,
                        notificationTitle,
                        item.getName(),
                        NotificationActionType.DELETE,
                        item.getId()
                );
                notifications.add(notification);
                GeneralValues.addInstitution(item);
            });
            if(!notifications.isEmpty()) {
                this.appRepository.getNotificationsRepository()
                        .saveAll(notifications);
            }
        });

        this.appRepository.getInstitutionRepository().deleteAll(institutions);

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted!")
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{id}")
    public ApiWorkFeedback deleteInstitution(@PathVariable("id") Long id,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Institution institutionToDelete = this.appRepository.getInstitutionRepository()
                .findById(id).orElse(null);
        if(institutionToDelete==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No Such Institution found.");
        }
        if(User.getMainChurchAdministrators().contains(loggedInUser.getUserType())){
            if(!institutionToDelete.getParentInstitutionId().equals(loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are not allowed to delete this church.");
            }
        }
        //


        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            institutionToDelete.preDelete(loggedInUser, this.appRepository, emailService);
        } else {
            this.doDeleteInstitution(loggedInUser, institutionToDelete);
        }

        String notificationTitle = "Deletion request sent "+institutionToDelete.getName()+" sent";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                institutionToDelete.getName(),
                NotificationActionType.DELETE,
                institutionToDelete.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted!")
                .build();

    }

    private void doDeleteInstitution(@Nullable User loggedInUser,
                                     @NonNull Institution institutionToDelete){
        this.deleteLogo(institutionToDelete, this.storageService);
        institutionToDelete.delete(this.appRepository);
        GeneralValues.removeInstitution(institutionToDelete);

        CompletableFuture.runAsync(()->{
            String notificationTitle = institutionToDelete.getName()+" deleted!";
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    notificationTitle,
                    institutionToDelete.getName(),
                    NotificationActionType.DELETE,
                    institutionToDelete.getId()
            );
            this.appRepository.getNotificationsRepository().save(notification);
        });
    }


    // Accessible from outside.
    @GetMapping("/confirm-deletion/{deletionToken}")
    public String confirmDeletion(@PathVariable String deletionToken){
        Institution institutionToDelete = this.appRepository.getInstitutionRepository()
                .findByDeletionToken(deletionToken);
        Date deletionTokenDeadline = institutionToDelete.getDeletionTokenDeadline();
        Date now = DateUtils.getNowDateTime();
        if(now.after(deletionTokenDeadline)) {
            this.doDeleteInstitution(null, institutionToDelete);
            return institutionToDelete.getName()+" deleted!";
        }

        return "Token expired!";
    }




    /**
     * Partner and Church
     */
    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/list-admins")
    public PageVO listAdmins(@RequestBody @Valid AdminListingQuery adminListingQuery,
                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(adminListingQuery, this.appRepository);
        adminListingQuery.setSize(Integer.MAX_VALUE);
        return User.listAdmins(this.getEntityManager(),
                this.appRepository,adminListingQuery);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/make-admin")
    public ApiWorkFeedback makeAdmin(@RequestBody @Validated({AdminCreatorValidator.class}) AdminCreator admin,
                                     @NonNull HttpSession session){
        InstitutionMember churchMember = this.appRepository.getInstitutionMemberRepository()
                .findById(admin.getMemberId()).orElse(null);
        if(churchMember==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Church member does not exist.");
        }
        admin.setFirstName(churchMember.getFirstName());
        admin.setLastName(churchMember.getLastName());
        admin.setInstitutionId(churchMember.getInstitutionId());
        admin.setPhone(churchMember.getPhone());
        admin.setGender(churchMember.getGender());
        admin.setEmail(churchMember.getEmail());
        admin.setAdminCode(churchMember.getCode());
        return this.saveAdmin(admin, session);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/save-admin")
    public ApiWorkFeedback saveAdmin(@RequestBody @Validated({AdminCreatorValidator.class}) AdminCreator admin,
                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(admin, this.appRepository);

        Institution institution = appRepository.getInstitutionRepository()
                .findById(admin.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No such Institution found.");
        }
        List<UserType> userTypes = new ArrayList<>();
        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            userTypes.add(UserType.CHURCH_ASSISTANT_ADMINISTRATOR);
            userTypes.add(UserType.CHURCH_BRANCH_ADMINISTRATOR);
            userTypes.add(UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR);
        } else{
            userTypes.add(UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR);
        }

        long currentAdminsCount = this.appRepository.getUsersRepository()
                .countAllAdmins(institution.getId(), userTypes);
        SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS.get(institution.getSubscriptionPlan());
        if(subscriptionPlan==null){
            String message = "Failed to load "+institution.getName()+" data.";
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    message);
        }

        if(currentAdminsCount >= subscriptionPlan.getAdmins()) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You have reached administrators quota, please contact the DNote to upgrade your plan.");
        }

        User adminToCreate = this.objectMapper.convertValue(admin, User.class);
        User savedAdmin;
        boolean isUpdate = admin.getId()!=null,
                setPassword = true;
        if(isUpdate){
            // This is an update
            savedAdmin = this.appRepository.getUsersRepository()
                    .findById(admin.getId())
                    .orElse(null);
            if(savedAdmin==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "No such admin found in this "+institution.getInstitutionTypeName()+".");
            }
            adminToCreate.setStaffId(savedAdmin.getStaffId());
            adminToCreate.setId(savedAdmin.getId());
            adminToCreate.setUsername(savedAdmin.getUsername());
            adminToCreate.setUserType(admin.getAdminType());
            if(AppStringUtils.isEmpty(adminToCreate.getPassword())){
                // password is empty
                setPassword = false;
                adminToCreate.setPassword(savedAdmin.getPassword());
            }
        } else {
            // Check no one is using that username;
            User userWithUsername = this.appRepository.getUsersRepository()
                    .findByUsername(admin.getUsername());
            if(userWithUsername!=null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Username not available.");
            }
            adminToCreate.setStaffId(admin.getAdminCode());
            adminToCreate.setUserType(admin.getAdminType());
        }
        if(setPassword) adminToCreate.setPassword(UpdatableBCrypt.hash(admin.getPassword()));
        savedAdmin = this.appRepository.getUsersRepository().save(adminToCreate);


        String notificationTitle = isUpdate ? "Updated " : "Created ";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedAdmin.getStaffId(),
                isUpdate ? NotificationActionType.UPDATE : NotificationActionType.CREATE,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        return  ApiWorkFeedback.builder()
                .message(isUpdate ? "Administrator Updated!" : "Administrator saved!")
                .successful(true)
                .object(savedAdmin)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/update-admin")
    public ApiWorkFeedback updateAdmin(@RequestBody @Validated({AdminUpdateValidator.class}) AdminCreator admin,
                                       @NonNull HttpSession session){
        return this.saveAdmin(admin, session);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @DeleteMapping("/delete-admin/{adminId}")
    public ApiWorkFeedback deleteAdmin(@PathVariable("adminId") Long adminId,
                                       @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        User savedAdmin = this.appRepository.getUsersRepository()
                .findById(adminId)
                .orElse(null);
        if(savedAdmin==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No such admin found.");
        }
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(savedAdmin.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution not recognized.");
        }
        if(institution.getEmail().equalsIgnoreCase(savedAdmin.getUsername())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "This is the primary administrator of "+institution.getName()+". Hence he/she can not be deleted.");
        }
        this.appRepository.getUsersRepository().deleteById(adminId);

        String notificationTitle = "Deleted Admin!";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedAdmin.getStaffId(),
                NotificationActionType.DELETE,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);


        return ApiWorkFeedback.builder()
                .message("Administrator deleted!")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/import-members")
    public ApiWorkFeedback importMembers(@RequestBody @Valid MemberImporter importer,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(importer, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(importer.getInstitutionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load institution data."));
        importer.setInstitution(institution);
        importer.setObjectMapper(this.objectMapper);
        importer.setLoggedInUser(loggedInUser);
        importer.setStorageService(this.storageService);
        importer.setEmailService(this.emailService);
        CompletableFuture.runAsync(()->
                importer.doImport(this.appRepository,
                        this.storageService,
                        loggedInUser)
        );

        return ApiWorkFeedback.builder()
                .message("Import may take a while. You will be notified when we finish importing.")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/import-donations")
    public ApiWorkFeedback importDonations(@RequestBody @Valid DonationsImporter importer,
                                           @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(importer, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(importer.getInstitutionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load institution data."));
        importer.setInstitution(institution);
        importer.setObjectMapper(this.objectMapper);
        importer.setLoggedInUser(loggedInUser);
        importer.setStorageService(this.storageService);
        importer.setEmailService(this.emailService);
        CompletableFuture.runAsync(()->
                importer.doImport(this.appRepository,
                        this.storageService,
                        loggedInUser)
        );
        return ApiWorkFeedback.builder()
                .message("Import may take a while. You will be notified when we finish importing.")
                .successful(true)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/import-families")
    public ApiWorkFeedback importFamilies(@RequestBody @Valid FamiliesImporter importer,
                                          @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(importer, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(importer.getInstitutionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load institution data."));
        importer.setInstitution(institution);
        importer.setObjectMapper(this.objectMapper);
        importer.setLoggedInUser(loggedInUser);
        importer.setStorageService(this.storageService);
        importer.setEmailService(this.emailService);
        CompletableFuture.runAsync(()->
                importer.doImport(this.appRepository,
                        this.storageService,
                        loggedInUser)
        );
        return ApiWorkFeedback.builder()
                .message("Import may take a while. You will be notified when we finish importing.")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/set-certificate")
    public ApiWorkFeedback setBackground(@RequestBody @Valid CertificateImporter importer,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(importer, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(importer.getInstitutionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load institution data."));
        institution.populateCertificates(this.appRepository);

        CertificateBackground certificate = this.appRepository
                .getCertificatesBackgroundsRepository()
                .findByInstitutionIdAndCertificateType(institution.getId(), importer.getType())
                .orElse(new CertificateBackground());

        String oldCertificate = null;
        if(certificate.getId()!=null){
            oldCertificate = certificate.getFileName();
        }
        certificate.setInstitutionId(importer.getInstitutionId());
        certificate.setFileName(importer.getFileName());
        certificate.setCertificateType(importer.getType());
        certificate.setFileName(importer.getFileName());
        certificate = this.appRepository
                .getCertificatesBackgroundsRepository().save(certificate);
        institution.getCertificates().put(certificate.getCertificateType(), certificate.getFileName());
        if(!StringUtils.isEmpty(oldCertificate)) this.storageService.delete(oldCertificate, ApplicationFileType.FILE);

        return ApiWorkFeedback.builder()
                .object(institution)
                .message("Certificate saved!")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
   

    @PostMapping("/extend-membership")
    public ApiWorkFeedback extendMembership(@RequestBody @Valid MembershipExtension membership,
                                            @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(membership.getInstitutionId()).orElseThrow(()->{
                    // throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "General Organization not recognized.");
                });

        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findByIdAndInstitutionId(membership.getMemberId(), membership.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No such member in "+ institution.getName()+".");
                });
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            // Means the Logged In User is an admin
            if(!Objects.equals(member.getInstitutionId(), loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You ave not right to edit this member.");
            }
        }
        member.setSubscription(membership.getDuration());
        member.adjustDeadline(true);
        InstitutionMember updatedMember = this.appRepository.getInstitutionMemberRepository().save(member);
        return ApiWorkFeedback.builder()
                .object(updatedMember)
                .message("Membership extended!")
                .build();
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/member-toggle-active")
    public ApiWorkFeedback memberToggleActive(@RequestBody @Valid MemberAction memberAction,
                                              @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(memberAction, this.appRepository);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(memberAction.getInstitutionId()).orElseThrow(()->{
                    // throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "General Organization not recognized.");
                });

        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findByIdAndInstitutionId(memberAction.getMemberId(), memberAction.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No such member in "+ institution.getName()+".");
                });
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            // Means the Logged In User is an admin
            if(!Objects.equals(member.getInstitutionId(), loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You avFe not right to edit this member.");
            }
        }
        boolean wasActive = member.isActive();
        member.toggleActive();
        InstitutionMember updatedMember = this.appRepository.getInstitutionMemberRepository().save(member);
        return ApiWorkFeedback.builder()
                .object(updatedMember)
                .message(wasActive ? "Deactivated!" : "Activated")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })



    @PostMapping("/add-member")
    public ApiWorkFeedback addMember(@RequestBody @Validated({
            ChurchMemberCreationValidator.class}) InstitutionMember member, // incoming 'member' object from frontend
                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        boolean isUpdate = member.getId()!=null; // check if it's an update operation
        member.checkPhoneIsProvided();
        loggedInUser.setPerformingUpdate(isUpdate);
        loggedInUser.checkUserFulfillsRequirements(member, this.appRepository);
        if(member.isNewPinCode()){
            CompletableFuture.runAsync(()->{
                PinCode newPinCode = new PinCode();
                newPinCode.setCountry(member.getCountry());
                newPinCode.setState(member.getState());
                newPinCode.setDistrict(member.getDistrict());
                newPinCode.setCode(member.getPincode());
                PinCode savedPincode = this.appRepository.getPincodeRepository().findByCode(member.getPincode()).orElse(null);
                if(savedPincode==null) {
                    this.appRepository.getPincodeRepository().save(newPinCode);
                    Notification notification = Notification.createNotification(loggedInUser, "New pincode saved",
                            "The newly saved pincode is "+ member.getPincode(),
                            NotificationActionType.CREATE, member.getInstitutionId());
                    this.appRepository.getNotificationsRepository().save(notification);
                }
            });
        }

        if(member.getCommunion()==null){
            member.setCommunionDate(null);
        } else {
            if(member.getCommunion() && member.getCommunionDate()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Communion date must not be null!");
            }
        }

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(member.getInstitutionId())
                .orElse(null);
        if(institution==null) throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "No Such Church/Organization found.");

        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(member.getInstitutionId()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Institution not specified.");
            }
        } else {
            if(institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                if (!member.getInstitutionId().equals(loggedInUser.getInstitutionId())) {
                    throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "You do not have the right to perform this operation within the given Organization.");
                }
            }
        }

        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            if(!member.getBaptized().equals(BaptismStatus.BAPTIZED)) member.setDateOfBaptism(null);
        }

        // --- இங்கே DUPLICATE CHECK LOGIC-ஐ சேர்த்துள்ளோம் ---
        if(member.getPhone()!=null || !StringUtils.isEmpty(member.getEmail())) {
            Long memberIdForCheck = null;
            if (isUpdate) { // If it's an update, pass the current member's ID to exclude it from the duplicate check
                memberIdForCheck = member.getId();
            }

            List<InstitutionMember> similarMembers = this.appRepository
                    .getInstitutionMemberRepository()
                    .getWithSimilarData(member.getPhone(), member.getEmail(), institution.getId(), memberIdForCheck); // <-- updated call
            if(similarMembers!=null && !similarMembers.isEmpty()){
                List<String> partnersCodes = similarMembers.parallelStream()
                        .map(InstitutionMember::getCode).collect(Collectors.toList());
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This member is already registered with such information. Code: "
                                + String.join(", ", partnersCodes));
            }
        }
        // --- DUPLICATE CHECK LOGIC முடிவு ---


        InstitutionMember memberToSave;

        if(isUpdate){ // இது update செய்யும் பகுதி
            InstitutionMember existingMemberInDb = this.appRepository
                    .getInstitutionMemberRepository()
                    .findById(member.getId())
                    .orElse(null);
            if(existingMemberInDb==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "The person you are trying to update does not exist.");
            }

            existingMemberInDb.setFirstName(member.getFirstName());
            existingMemberInDb.setLastName(member.getLastName());
            existingMemberInDb.setGender(member.getGender());
            existingMemberInDb.setDob(member.getDob());
            existingMemberInDb.setDom(member.getDom());
            existingMemberInDb.setEmail(member.getEmail());
            existingMemberInDb.setPhone(member.getPhone());
            existingMemberInDb.setPhoneCode(member.getPhoneCode());
            existingMemberInDb.setAlternatePhone(member.getAlternatePhone());
            existingMemberInDb.setAlternatePhoneCode(member.getAlternatePhoneCode());
            existingMemberInDb.setWhatsappNumber(member.getWhatsappNumber());
            existingMemberInDb.setWhatsappNumberCode(member.getWhatsappNumberCode());
            existingMemberInDb.setLandlinePhone(member.getLandlinePhone());
            existingMemberInDb.setLandlinePhoneCode(member.getLandlinePhoneCode());
            existingMemberInDb.setAddressLine1(member.getAddressLine1());
            existingMemberInDb.setAddressLine2(member.getAddressLine2());
            existingMemberInDb.setAddressLine3(member.getAddressLine3());
            existingMemberInDb.setPincode(member.getPincode());
            existingMemberInDb.setState(member.getState());
            existingMemberInDb.setDistrict(member.getDistrict());
            existingMemberInDb.setCountry(member.getCountry());
            existingMemberInDb.setLanguage(member.getLanguage());
            existingMemberInDb.setProfile(member.getProfile());
            existingMemberInDb.setBaptized(member.getBaptized());
            existingMemberInDb.setCommunion(member.getCommunion());
            existingMemberInDb.setCommunionDate(member.getCommunionDate());
            existingMemberInDb.setDateOfBaptism(member.getDateOfBaptism());
            existingMemberInDb.setChurchFunction(member.getChurchFunction());
            existingMemberInDb.setDeadline(member.getDeadline());

            existingMemberInDb.setSubscription(member.getSubscription());

            if (member.getCategoriesIds() != null) {
                existingMemberInDb.setCategoriesIds(member.getCategoriesIds());
            } else {
                existingMemberInDb.setCategoriesIds(new HashSet<>());
            }

            existingMemberInDb.setUpdateDate(DateUtils.getNowDateTime());

            if(!Objects.equals(member.getProfile(), existingMemberInDb.getProfile())){
                if(!StringUtils.isEmpty(existingMemberInDb.getProfile())){
                    this.storageService.delete(existingMemberInDb.getProfile(), ApplicationFileType.FILE);
                }
            }
            memberToSave = existingMemberInDb;
        } else { // இது புதிய member create செய்யும் பகுதி
            member.setCreationDate(DateUtils.getNowDateTime());
            member.adjust();

            SubscriptionPlan subscription = GeneralValues.SUBSCRIPTIONS.get(institution.getSubscriptionPlan());
            if(subscription==null){
                String message = "Failed to load ";
                message += institution.getInstitutionTypeName()+"'s info";
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, message);
            }
            Long institutionMembers = this.appRepository.getInstitutionMemberRepository()
                    .countAllByInstitutionId(institution.getId());
            if(subscription.getMembers() <= institutionMembers){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Quota reached. Please contact DNote to upgrade your plan.");
            }

            member.computeMemberCode(institution, this.appRepository);

            Long membersWithSameCode = this.appRepository.getInstitutionMemberRepository()
                    .countByCodeAndInstitutionId(member.getCode(), institution.getId());
            if(membersWithSameCode==null) membersWithSameCode = 0L;
            if(membersWithSameCode>0){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Code provided is not available.");
            }

            member.setInstitutionId(institution.getId());
            memberToSave = member;
        }

        InstitutionMember newMember = memberToSave.save(this.appRepository, institution);

        if (member.isFamilyHead()) {
            String familyCode = institution.getInstitutionType().equals(InstitutionType.CHURCH)
                    ? null
                    : institution.generateFamilyCode(this.appRepository);
            // newMember.createFamily(institution, appRepository, familyCode);
        }

        if(!isUpdate) {
            if (newMember.getCategoriesIds() != null && !newMember.getCategoriesIds().isEmpty()) {
                List<PeopleSubscription> subscriptions = new ArrayList<>();
                final InstitutionMember finalNewMember = newMember;
                newMember.getCategoriesIds().parallelStream()
                        .forEach(singleCategory -> {
                            PeopleSubscription peopleSubscription = new PeopleSubscription();
                            peopleSubscription.setInstitutionId(institution.getId());
                            peopleSubscription.setSubscription(finalNewMember.getSubscription());
                            peopleSubscription.setMemberId(finalNewMember.getId());
                            peopleSubscription.setCategoryId(singleCategory);
                            peopleSubscription.computeDeadline(finalNewMember.getSubscription());
                            subscriptions.add(peopleSubscription);
                        });

                List<PeopleSubscription> madeSubscriptions = this.appRepository.getPeopleSubscriptionRepository().saveAll(subscriptions);
                // newMember.setMemberSubscriptions(madeSubscriptions);
            }
        }
        newMember = InstitutionMember.populateSearchedMember(Collections.singletonList(newMember), this.appRepository, institution)
                .get(0);


        String notificationTitle = (isUpdate ? "Updated " : "Created ")
                + (institution.getInstitutionType().equals(InstitutionType.GENERAL) ? "Partner" : "Member");
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                newMember.getCode(),
                isUpdate ? NotificationActionType.UPDATE : NotificationActionType.CREATE,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        if(!isUpdate) this.appRepository.getInstitutionRepository().save(institution);

        MemberActivity.saveActivity(this.appRepository,
                newMember.getId(),
                institution.getId());
        return ApiWorkFeedback.builder()
                .message(institution.getMemberTitleName()+(isUpdate ? " updated!" : " saved!"))
                .successful(true)
                .object(newMember)
                .build();
    }
    
    
    
    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/list-members")
    public PageVO listMember(@RequestBody @Valid MemberListingQuery listQuery,
                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(listQuery, this.appRepository);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(listQuery.getInstitutionId())
                .orElseThrow(()->{
                    // Return an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized.");
                });
        listQuery.setInstitution(institution);
        Page<InstitutionMember> pageOfMembers = InstitutionMember
                .getListOfMembers(this.getEntityManager(),
                        this.appRepository, objectMapper,
                        listQuery);
        return PageVO.getPageVO(pageOfMembers);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/add-partner")
    public ApiWorkFeedback addPartner(@RequestBody @Validated({
            PartnerCreationValidator.class}) InstitutionMember member,
                                      @NonNull HttpSession session){
        return this.addMember(member, session);
    }




    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @DeleteMapping("/delete-member/{memberId}")
    public ApiWorkFeedback deleteMember(@PathVariable("memberId") Long memberId,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        InstitutionMember savedMember = this.appRepository
                .getInstitutionMemberRepository()
                .findById(memberId)
                .orElse(null);
        if(savedMember==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The person you are trying to delete does not exist.");
        }

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(savedMember.getInstitutionId())
                .orElseThrow(()->{
                    // Return an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized.");
                });
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                if (!savedMember.getInstitutionId().equals(loggedInUser.getInstitutionId())) {
                    throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "You are not allowed to delete this Person.");
                }
            } else {
                loggedInUser.patchInstitutionIdInUse(savedMember, this.appRepository);
            }
        }

        this.appRepository.getInstitutionMemberRepository()
                .deleteById(memberId);

        String notificationTitle = "Deleted "
                + (institution.getInstitutionType().equals(InstitutionType.GENERAL) ? "Partner" : "Member");
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedMember.getCode(),
                NotificationActionType.DELETE,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Deleted.")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/save-family-prospectus")
    public ApiWorkFeedback saveProspectusMember(@RequestBody @Valid ProspectusMember prospectusMember,
                                                @NonNull HttpSession session,
                                                BindingResult bindingResult)
            throws MethodArgumentNotValidException {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(prospectusMember, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(prospectusMember.getInstitutionId())
                .orElseThrow(()->{
                    String message = "Institution not recognized.";
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, message);
                });
        if(!institution.isChurch() && prospectusMember.getSubscription()==null){
            bindingResult.reject("subscription", "Subscription must be specified!");
        }
        if(!StringUtils.isEmpty(prospectusMember.getEmail())){
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if(!prospectusMember.getEmail().matches(emailRegex)){
                bindingResult.reject("email", "Email Should be valid!");
            }
        }

        if(bindingResult.hasErrors()) throw new MethodArgumentNotValidException(null, bindingResult);

        institution.fetchSubscriptionPlan(this.appRepository);
        if(!institution.canAddMembers(this.appRepository)){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Family Quota exceeded. Please upgrade your plan.");
        }

        InstitutionMember member = new InstitutionMember();
        BeanUtils.copyProperties(prospectusMember, member);
        InstitutionMember savedMember = member.save(appRepository, institution);
        CompletableFuture.runAsync(()->{
            Notification notification = Notification
                    .createNotification(loggedInUser,
                            "Family Member Creation",
                            member.getFullName(),
                            NotificationActionType.CREATE,
                            institution.getId());
            this.appRepository.getNotificationsRepository().save(notification);
        });
        return ApiWorkFeedback.builder()
                .message("Member saved!")
                .successful(true)
                .object(savedMember)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/save-family")
    public ApiWorkFeedback saveFamily(@RequestBody @Validated({ProspectusMemberValidation.class}) FamilyCreatorModel familyCreatorModel,
                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
///
        boolean isUpdate = familyCreatorModel.getId() != null && familyCreatorModel.getId() != 0L;
        loggedInUser.setPerformingUpdate(isUpdate);
        loggedInUser.checkUserFulfillsRequirements(familyCreatorModel, this.appRepository);

        InstitutionMember familyHead = this.appRepository
                .getInstitutionMemberRepository()
                .findById(familyCreatorModel.getFamilyHead())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Specified Family head is not recognized."));

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(familyCreatorModel.getInstitutionId())
                .orElseThrow(()->{
                    String message = "Institution not recognized.";
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, message);
                });

        InstitutionFamily newFamily = new InstitutionFamily(); // This will become savedFamily later
        if(isUpdate){
            newFamily = this.appRepository.getInstitutionFamilyRepository()
                    .findById(familyCreatorModel.getId()).orElse(null);
            if(newFamily==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Family not found.");
            }
            newFamily.setLastUpdate(DateUtils.getNowDateTime());
            if(!Objects.equals(newFamily.getPhoto(), familyCreatorModel.getPhoto())){
                if(!StringUtils.isEmpty(newFamily.getPhoto())){
                    this.storageService.delete(newFamily.getPhoto(), ApplicationFileType.FILE);
                }
            }

            List<Long> existingMembers = new ArrayList<>();
            if(familyCreatorModel.getMembers()!=null) {
                existingMembers = familyCreatorModel.getMembers()
                        .parallelStream()
                        .map(FamilyMemberModel::getMemberId).filter(Objects::nonNull)
                        .collect(Collectors.toList());
                this.appRepository.getInstitutionFamilyMemberRepository()
                        .deleteNonExistingMembers(institution.getId(),
                                newFamily.getId(),
                                existingMembers);
            }
        } else {
            institution.fetchSubscriptionPlan(this.appRepository);
            if(!institution.canCreateFamily(this.appRepository)){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Family Quota exceeded. Please upgrade your plan.");
            }
        }

        // Set properties on the newFamily object (which will be saved as savedFamily)
        newFamily.setName(familyHead.getFullName()); // Your original code already sets this
        newFamily.setInstitutionId(familyCreatorModel.getInstitutionId());
        newFamily.setPhone(familyCreatorModel.getPhone());
        newFamily.setAddressLine1(familyCreatorModel.getAddressLine1());
        newFamily.setAddressLine2(familyCreatorModel.getAddressLine2());
        newFamily.setAddressLine3(familyCreatorModel.getAddressLine3());
        newFamily.setPincode(familyCreatorModel.getPincode());
        newFamily.setState(familyCreatorModel.getState());
        newFamily.setDistrict(familyCreatorModel.getDistrict());
        newFamily.setDob(familyCreatorModel.getDob());
        newFamily.setFamilyHead(familyHead.getId()); // Your original code already sets this
        newFamily.setPhoto(familyCreatorModel.getPhoto());
        // ADD THIS LINE: Set hofCode directly from familyHead
        newFamily.setHofCode(familyHead.getCode()); // Add this line to set the hofCode

        InstitutionFamily savedFamily = this.appRepository.getInstitutionFamilyRepository()
                .save(newFamily); // This saves the family details to DB

        String notificationTitle = (isUpdate ? "Updated ": "Created ")+"Family";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                newFamily.getName(), // Use newFamily.getName() as it's already set from familyHead
                isUpdate ? NotificationActionType.UPDATE : NotificationActionType.CREATE,
                newFamily.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        // Process and save/update family members in the 'institution_family_members' table
        familyCreatorModel.getMembers()
                .parallelStream()
                .forEach(singleMember-> this.appRepository.getInstitutionMemberRepository()
                        .findById(singleMember.getMemberId())
                        .ifPresent(member->{
                            InstitutionFamilyMember savedMember = this.appRepository.getInstitutionFamilyMemberRepository()
                                    .findByMemberIdAndFamilyId(member.getId(), savedFamily.getId());
                            if(savedMember==null) {
                                InstitutionFamilyMember familyMember = new InstitutionFamilyMember();
                                familyMember.setFamilyId(savedFamily.getId());
                                familyMember.setInstitutionId(savedFamily.getInstitutionId());
                                familyMember.setMemberId(member.getId());
                                familyMember.setTitle(singleMember.getTitle()); // Title is correctly set here for DB
                                this.appRepository.getInstitutionFamilyMemberRepository()
                                        .save(familyMember);
                            } else {
                                savedMember.setTitle(singleMember.getTitle()); // Title is correctly updated here for DB
                                this.appRepository.getInstitutionFamilyMemberRepository()
                                        .save(savedMember);
                            }
                            // **** IMPORTANT FIX: REMOVE THIS LINE ****
                            // savedFamily.addMember(member); // <--- DELETE THIS LINE!
                            // This line was adding members to the in-memory object without their roles populated from DB
                        })
                );

        // **** CRITICAL FIX: Populate members and their roles into the savedFamily object from the DB ****
        // After all database operations for members are complete,
        // call populateMembers to load the latest data (including correct roles) into the 'savedFamily' object.
        savedFamily.populateMembers(this.appRepository); // <<< ADD THIS LINE HERE

        // Return the updated 'savedFamily' object which now has correctly populated members and their roles.
        return ApiWorkFeedback.builder()
                .message("Family Saved Successfully!") // Changed message to be more generic
                .successful(true)
                .object(savedFamily) // This 'savedFamily' should now have correct familyRole values
                .build();
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/list-families")
    public PageVO listFamilies(@RequestBody @Valid FamilyListQuery listQuery,
                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(listQuery, this.appRepository);
        Page<InstitutionFamily> foundFamilies = InstitutionFamily.findFamilies(this.getEntityManager(),
                this.appRepository,
                listQuery);
        return PageVO.getPageVO(foundFamilies);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @DeleteMapping("/delete-family/{familyId}")
    public ApiWorkFeedback deleteFamily(@PathVariable("familyId") Long familyId,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        InstitutionFamily family = this.appRepository.getInstitutionFamilyRepository()
                .findById(familyId).orElse(null);
        if(family==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The family you are trying to delete does not exist.");
        }
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(family.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Family Institution not recognized.");
                });
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                if (!family.getInstitutionId().equals(loggedInUser.getInstitutionId())) {
                    throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "You are not allowed to delete this family.");
                }
            } else {
                loggedInUser.patchInstitutionIdInUse(family ,this.appRepository);
            }
        }
        this.appRepository.getInstitutionFamilyMemberRepository()
                .deleteAllByFamilyId(familyId);

        this.appRepository.getInstitutionFamilyRepository().deleteById(familyId);
        String notificationTitle = "Deleted Family!";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                family.getName(),
                NotificationActionType.DELETE,
                family.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Family deleted!")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/search-prospective-family-member")
    public List<InstitutionMember> searchInstitutionMember(@RequestBody FamilyProspectiveMemberSearcher searcher,
                                                           @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(searcher.getInstitutionId()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "No institution specified.");
            }
        } else {
            loggedInUser.patchInstitutionIdInUse(searcher, this.appRepository);
        }
        return this.appRepository.getInstitutionMemberRepository()
                .searchMemberByNameOrSearchCode(searcher.getCode(),
                        searcher.getCode().toLowerCase(),
                        searcher.getInstitutionId());
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,

    })
    @PostMapping("/make-donation")
    public ApiWorkFeedback makeDonation(@RequestBody @Validated({GeneralDonationValidation.class}) DonationContribution theDonation,
                                        @NonNull HttpSession session){
        String notificationTitle = "Donation failed!";
        InstitutionReceipt savedReceipt = null;
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(theDonation, appRepository);
        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findById(theDonation.getMemberId()).orElse(null);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No Such Partner/Member found.");
        }
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(member.getInstitutionId()).orElse(null);
        if (institution == null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution not recognized.");
        }
        InstitutionReceipt receipt = new InstitutionReceipt();
        receipt.setMemberId(member.getId());
        receipt.setInstitutionId(member.getInstitutionId());
        String receiptNo = institution.generateReceiptNo(this.appRepository, null);
        receipt.setReceiptNo(receiptNo);
        receipt.setRemarks(theDonation.getRemarks());
        receipt.setCreditAccountId(theDonation.getCreditAccount());
        receipt.setReferenceAccount(theDonation.getReferenceAccount());
        receipt.setBankReference(theDonation.getBankReference());
        receipt.setReferenceNo(theDonation.getReferenceNo());
        receipt.setPaymentMode(theDonation.getPaymentMode());
        receipt.setEntryDate(theDonation.getEntryDate());
        savedReceipt = receipt.save(this.appRepository, institution);
        NotificationActionType notificationType = NotificationActionType.DELETE;
        if (savedReceipt != null) {
            InstitutionReceipt finalSavedReceipt = savedReceipt;
            theDonation.getDonations().parallelStream()
                    .forEach(donationModel -> {
                        InstitutionDonation donation = new InstitutionDonation();
                        donation.setMemberId(member.getId());
                        donation.setEntryDate(theDonation.getEntryDate());
                        donation.setAmount(donationModel.getAmount());
                        donation.setCategoryId(donationModel.getCategoryId());
                        donation.setInstitutionId(member.getInstitutionId());
                        donation.setReceiptNo(finalSavedReceipt.getReceiptNo());
                        InstitutionDonation institutionDonation = this.appRepository
                                .getInstitutionDonationRepository()
                                .save(donation);
                        finalSavedReceipt.addDonation(institutionDonation);
                    });

            notificationTitle = "Donation Made";
            notificationType = NotificationActionType.CREATE;

            member.subscribe(this.appRepository, institution, theDonation);

            InstitutionSimpleVO institutionSimpleVO = this.objectMapper.convertValue(institution,
                    InstitutionSimpleVO.class);
            savedReceipt.setInstitution(institutionSimpleVO);
        }
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                member.getFirstName() + " " + member.getLastName(),
                notificationType,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        MemberActivity.saveActivity(this.appRepository,
                member.getId(),
                institution.getId());

        return ApiWorkFeedback.builder()
                .message(notificationTitle)
                .successful(savedReceipt!=null)
                .object(savedReceipt)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get-single-donation")
    public ApiWorkFeedback getSingleDonation(@RequestBody @Valid ReceiptRequester requester,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(requester, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(requester.getInstitutionId())
                .orElseThrow(()->{
                    // Return an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No Such Institution Found.");
                });
        InstitutionReceipt savedReceipt = this.appRepository.getInstitutionReceiptsRepository()
                .findByReceiptNoAndInstitutionId(requester.getReceiptCode(), institution.getId())
                .orElseThrow(()->{
                    //Throw back an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No such receipt found.");
                });
        savedReceipt.setInstitutionOriginal(institution);
        savedReceipt.populate(this.appRepository, false);
        InstitutionSimpleVO institutionSimpleVO = this.objectMapper.convertValue(institution,
                InstitutionSimpleVO.class);
        savedReceipt.setInstitution(institutionSimpleVO);
        return ApiWorkFeedback.builder()
                .message("Receipt loaded")
                .object(savedReceipt)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-partner-donations")
    public PartnerDonationVO getMemberDonations(@RequestBody @Valid DonationListQuery donationListQuery,
                                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(donationListQuery, this.appRepository);

        if(StringUtils.isEmpty(donationListQuery.getReceiptNo())
                && donationListQuery.getMemberId()==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Please Specify at least Member of Receipt No.");
        }

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(donationListQuery.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Institution not loaded.");
        }

        if (!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            boolean allowed;
            if (User.getMainChurchAdministrators().contains(loggedInUser.getUserType())
                    && institution.getParentInstitutionId() != null) {
                // Raba ko institution iri muma institution uyu mutype atwara
                Institution parentInstitution = this.appRepository.getInstitutionRepository()
                        .findById(institution.getParentInstitutionId())
                        .orElseThrow(() -> {
                            // Throw an error
                            return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                    "Could not fetch Main Branch data.");
                        });
                allowed = parentInstitution.getId().equals(loggedInUser.getInstitutionId());
            } else {
                // Banditswe hamwe
                allowed = donationListQuery.getInstitutionId().equals(loggedInUser.getInstitutionId());
            }

            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are not allowed to perform this operation.");
            }
        }
        InstitutionSimpleVO institutionVO = this.objectMapper.convertValue(institution,
                InstitutionSimpleVO.class);

        donationListQuery.setInstitution(institution);

        List<UserType> singleDonationViewers = Arrays.asList(UserType.CHURCH_DATA_ENTRY_OPERATOR,
                UserType.ORGANIZATION_DATA_ENTRY_OPERATOR);
        if (singleDonationViewers.contains(loggedInUser.getUserType()))  donationListQuery.setSize(1);
        PartnerDonationVO receipts = InstitutionReceipt
                .getMemberReceipts(this.getEntityManager(),
                        appRepository,
                        donationListQuery,
                        institutionVO);

        PageVO receiptsVO = PageVO.getPageVO(receipts.getReceiptsOriginal());
        receipts.setReceipts(receiptsVO);

        if (!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            institution.incrementReceiptGenerations();
        }
        this.appRepository.getInstitutionRepository().save(institution);
        return receipts;
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete-donation/{donationId}")
    public ApiWorkFeedback deleteDonation(@PathVariable("donationId") Long donationId,
                                          @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);

        InstitutionReceipt receipt = this.appRepository
                .getInstitutionReceiptsRepository()
                .findById(donationId).orElse(null);
        if(receipt==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Donation not found.");
        }
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(receipt.getInstitutionId())
                .orElseThrow(()->{
                    // throw error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No such institution found!");
                });
        InstitutionMember donator = this.appRepository
                .getInstitutionMemberRepository()
                .findById(receipt.getMemberId()).orElse(null);

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!receipt.getInstitutionId().equals(loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are not allowed this donation.");
            }
        }
        receipt.delete(this.appRepository);

        String notificationTitle = "Donation Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                (donator==null) ? "Someone" : donator.getFirstName()+" "+donator.getLastName(),
                NotificationActionType.DELETE,
                institution.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Donation deleted.")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/set-church-role")
    public ApiWorkFeedback setChurchRole(@RequestBody @Valid PastorCreatorModel pastorCreatorModel,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(pastorCreatorModel, this.appRepository);

        InstitutionMember saved = this.appRepository.getInstitutionMemberRepository()
                .findById(pastorCreatorModel.getMemberId()).orElse(null);
        if(saved==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Member not found.");
        }
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(saved.getInstitutionId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Member's Church does not exist."));

        if(!institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Only Church Members are allowed to have church roles.");
        }

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!loggedInUser.getInstitutionId().equals(saved.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are only allowed to set within your institution.");
            }
        }
        this.appRepository.getInstitutionMemberRepository().unsetPriests(institution.getId());
        saved.setChurchFunction(ChurchFunction.PRIEST);
        this.appRepository.getInstitutionMemberRepository().save(saved);

        String notificationTitle = "Set Church Role";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                saved.getFirstName()+" "+saved.getLastName(),
                NotificationActionType.UPDATE,
                saved.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        MemberActivity.saveActivity(this.appRepository,
                saved.getId(),
                institution.getId());
        return ApiWorkFeedback
                .builder()
                .successful(true)
                .object(saved)
                .message("Pastor Created")
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-addresses")
    public PageVO getAddresses(@RequestBody @Valid AddressRequestModel addressRequestModel,
                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(addressRequestModel, this.appRepository);
        addressRequestModel.adjust();
        addressRequestModel.setSize(Integer.MAX_VALUE);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(addressRequestModel.getInstitutionId())
                .orElseThrow(()->{
                    // Return an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Failed to load institution data");
                });
        addressRequestModel.setInstitution(institution);
        Page<InstitutionMember> page = InstitutionMember.getMembersAddresses(this.getEntityManager(),
                this.appRepository, addressRequestModel);

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            institution.acknowledgeAddressPrinting(page.getTotalElements());
        }
        this.appRepository.getInstitutionRepository().save(institution);

        this.acknowledgeDataGeneration(loggedInUser, addressRequestModel, true);
        return PageVO.getPageVO(page);
    }

    private void acknowledgeDataGeneration(final User loggedInUser,
                                           final AddressRequestModel addressRequestModel,
                                           final boolean isAddress){
        List<UserType> usersToReports = Arrays.asList(UserType.values());
        if(usersToReports.contains(loggedInUser.getUserType())) {
            CompletableFuture.runAsync(() -> {
                Map<String, String> map = new HashMap<>();
                if(!StringUtils.isEmpty(addressRequestModel.getQuery())) map.put("Query", addressRequestModel.getQuery().trim());
                if(addressRequestModel.getCountry()!=null && !addressRequestModel.getCountry().isEmpty()){
                    map.put("Query", String.join(", ", addressRequestModel.getCountry()));
                }
                if(!StringUtils.isEmpty(addressRequestModel.getDistrict())) map.put("District", addressRequestModel.getDistrict().trim());
                if(addressRequestModel.getPincode()!=null) map.put("PinCode", addressRequestModel.getPincode().toString());
                if(addressRequestModel.getPaymentMode()!=null && !addressRequestModel.getPaymentMode().isEmpty()){
                    List<String> paymentModes = addressRequestModel.getPaymentMode()
                            .parallelStream()
                            .map(PaymentMode::getDisplayName)
                            .collect(Collectors.toList());
                    map.put("Payment Mode", String.join(", ", paymentModes));
                }
                if(addressRequestModel.getMinAmount()!=null) map.put("Min Amount", addressRequestModel.getMinAmount().toString());
                if(addressRequestModel.getMaxAmount()!=null) map.put("Max Amount", addressRequestModel.getMaxAmount().toString());
                StringBuilder stringBuilder = new StringBuilder();
                for(Map.Entry<String, String> singleEntry : map.entrySet()){
                    stringBuilder.append(singleEntry.getKey())
                            .append(" : ")
                            .append(singleEntry.getValue())
                            .append(" ");
                }
                Notification notification = Notification.createNotification(loggedInUser,
                        loggedInUser.getFullName()+": "+loggedInUser.getUsername()+" generated a "+(isAddress ? "Addresses" : "Report"),
                        "Filters: "+stringBuilder,
                        NotificationActionType.READ,
                        addressRequestModel.getInstitutionId());
                this.appRepository.getNotificationsRepository().save(notification);
            });
        }
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-report")
    public Receiptor getReport(@RequestBody @Valid AddressRequestModel addressRequestModel,
                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(addressRequestModel, this.appRepository);
        addressRequestModel.adjust();
        addressRequestModel.setSize(Integer.MAX_VALUE);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(addressRequestModel.getInstitutionId())
                .orElse(null);
        if(institution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No institution Specified.");
        }

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            institution.incrementReportsGenerations();
        }
        this.appRepository.getInstitutionRepository().save(institution);

        this.acknowledgeDataGeneration(loggedInUser, addressRequestModel, false);

        return InstitutionReceipt.getReport(this.getEntityManager(),
                this.appRepository,
                addressRequestModel,
                institution);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-single-institution")
    public Institution getSingleInstitution(@RequestBody @Valid SingleInstitutionRequest singleInstitutionRequest ,
                                            @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(singleInstitutionRequest, this.appRepository);
        return this.appRepository.getInstitutionRepository()
                .findById(singleInstitutionRequest
                        .getInstitutionId()).orElseThrow(()->
                        new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "No such Institution Found."));
    }




    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-priest-signature")
    public ApiWorkFeedback getSingleInstitution(@RequestBody @Valid PriestSignatureRequestModel priestSignatureRequestModel,
                                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(priestSignatureRequestModel, this.appRepository);
        PriestSignature optionalInstitution =
                this.appRepository.getPriestSignatureRepository()
                        .findByInstitutionIdAndMemberId(
                                priestSignatureRequestModel.getInstitutionId(),
                                priestSignatureRequestModel.getPriestId());

        return  ApiWorkFeedback
                .builder()
                .successful(true)
                .object(optionalInstitution)
                .message("Priest signature Loading")
                .build();
    }





    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-single-family")
    public ApiWorkFeedback getSingleFamily(@RequestBody @Valid SingleInstitutionRequest singleInstitutionRequest ,
                                           @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(singleInstitutionRequest, this.appRepository);
        InstitutionFamily optionalInstitution =
                this.appRepository.getInstitutionFamilyRepository()
                        .findInstitutionFamilyByInstitutionId(singleInstitutionRequest
                                .getInstitutionId());
        return  ApiWorkFeedback
                .builder()
                .successful(true)
                .object(optionalInstitution)
                .message("Family Institution  Loading")
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-receipt-configuration")
    public ApiWorkFeedback save(@RequestBody @Valid ReceiptModel receiptModel,
                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(receiptModel, this.appRepository);
        receiptModel.check();

        Institution savedInstitution = this.appRepository
                .getInstitutionRepository().findById(receiptModel.getInstitutionId())
                .orElse(null);
        if(savedInstitution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "The institution does exist.");
        }
        savedInstitution.setReceiptMessage(receiptModel.getReceiptMessage());
        savedInstitution.setBibleVerse(receiptModel.getBibleVerse());
        savedInstitution.setReceiptTemplate(receiptModel.getReceiptTemplate());
        savedInstitution.setReceiptPhone(receiptModel.getReceiptPhone());
        savedInstitution.setUpi(receiptModel.getUpi());
        savedInstitution.setCustomReceiptTemplate(receiptModel.getCustomReceiptTemplate());
        savedInstitution.setCustomModel(receiptModel.getCustomModel());

        SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS
                .get(savedInstitution.getSubscriptionPlan());
        if(subscriptionPlan==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Subscription plan not found.");
        }
        Institution save = this.appRepository.getInstitutionRepository().save(savedInstitution);
        savedInstitution.setAllowedFeatures(subscriptionPlan.getFeatures());
        String message = (receiptModel.getInstitutionId()!=null)   ? "Updated" : "Saved";
        String notificationMessage  = "Receipt Configurations.";
        Notification  notification  = Notification.createNotification(loggedInUser, notificationMessage,
                "The Receipt "+ savedInstitution.getReceiptTemplate() + " has been saved." ,
                NotificationActionType.CREATE, savedInstitution.getId());
        this.appRepository.getNotificationsRepository().save(notification);
        return  ApiWorkFeedback
                .builder()
                .message(message)
                .successful(true)
                .object(save)
                .build();
    }




    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-receipt-configuration")
    public Institution getReceipt(@RequestBody @Valid ReceiptListModel receiptListModel,
                                  @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(receiptListModel, this.appRepository);
        Institution savedInstitution = this.appRepository
                .getInstitutionRepository().findById(receiptListModel.getInstitutionId())
                .orElse(null);
        if(savedInstitution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "The institution does exist.");
        }
        return this.appRepository.getInstitutionRepository()
                .findById(receiptListModel.getInstitutionId()).orElse(null);

    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-accounts")
    public ApiWorkFeedback saveAccounts(@Valid @RequestBody AccountsCreator model,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);
        model.getCreditAccounts().forEach(account->{
            account.setInstitutionId(model.getInstitutionId());
        });
        Iterable<InstitutionCreditAccount> savedAccounts = this.appRepository
                .getCreditAccountsRepository()
                .saveAll(model.getCreditAccounts());

        return ApiWorkFeedback.builder()
                .message("Saved")
                .objects(Lists.newArrayList(savedAccounts))
                .successful(true)
                .build();
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get-accounts")
    public List<InstitutionCreditAccount> getAccount(@Valid @RequestBody SimpleGetModel model,
                                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);

        List<Long> institutionIds = new ArrayList<>();
        institutionIds.add(model.getInstitutionId());
        return this.appRepository.getCreditAccountsRepository()
                .findAllByInstitutionIdIn(institutionIds);
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,

            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete-account/{accountId}")
    public ApiWorkFeedback getAccount(@PathVariable("accountId") Long accountId){
        Long receiptInUse = this.appRepository.getInstitutionReceiptsRepository()
                .countAllByCreditAccountId(accountId);
        if(receiptInUse!=null && receiptInUse>0){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Account in Use: You can not delete this Credit Account.");
        }
        this.appRepository.getCreditAccountsRepository()
                .deleteById(accountId);
        return ApiWorkFeedback.builder()
                .message("Deleted!")
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update-account")
    public ApiWorkFeedback getAccount(@Valid @RequestBody InstitutionCreditAccount account){
        if(account.getId()==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Some information of this Credit account are missing.");
        }
        InstitutionCreditAccount savedAccount = this.appRepository.getCreditAccountsRepository()
                .findById(account.getId())
                .orElse(null);
        if(savedAccount==null){
            throw new ResponseStatusException(
                    HttpStatus.EXPECTATION_FAILED,
                    "No Such Credit Account Found.");
        }
        savedAccount.setName(account.getName());
        InstitutionCreditAccount updatedAccount = this.appRepository.getCreditAccountsRepository()
                .save(savedAccount);
        return ApiWorkFeedback.builder()
                .message("Updated.")
                .object(updatedAccount)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/acknowledge-fund-receipts")
    public ApiWorkFeedback informFormReceipt(@RequestBody @Valid ReceiptAcknowledger voicer,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);

        InstitutionReceipt receipt = this.appRepository.getInstitutionReceiptsRepository()
                .findById(voicer.getReceiptId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "No such receipt found."));
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!loggedInUser.getInstitutionId().equals(receipt.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are not allowed to perform this operation.");
            }
        }

        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findById(receipt.getMemberId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load donator/contributor details."));

        Institution institution = this.appRepository.getInstitutionRepository().findById(receipt.getInstitutionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Institution not loaded properly."));

        receipt.setMember(member);
        InstitutionSimpleVO institutionSimpleVO = this.objectMapper
                .convertValue(institution,
                        InstitutionSimpleVO.class);
        receipt.setInstitution(institutionSimpleVO);
        receipt.setInstitutionOriginal(institution);
        receipt.populate(this.appRepository, false);

        List<InstitutionDonation> donations = this.appRepository.getInstitutionDonationRepository()
                .findAllByReceiptNoAndInstitutionId(receipt.getReceiptNo(), institution.getId());
        List<Long> subscriptionsIds = donations.parallelStream().map(item-> institution.getInstitutionType().equals(InstitutionType.GENERAL)
                ? item.getCategoryId()
                : item.getChurchContributionId()).collect(Collectors.toList());
        if(subscriptionsIds.isEmpty()){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Receipt not valid!");
        }
        List<String> subscriptionsNames = (institution.getInstitutionType().equals(InstitutionType.GENERAL))
                ? this.appRepository.getCategoryRepository()
                .findAllByIdIn(subscriptionsIds).parallelStream().map(InstitutionCategory::getName).collect(Collectors.toList())
                : this.appRepository.getChurchContributionRepository()
                .findAllByIdIn(subscriptionsIds).parallelStream().map(ChurchContribution::getName).collect(Collectors.toList());
        if(subscriptionsNames.isEmpty()){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    (institution.getInstitutionType().equals(InstitutionType.GENERAL) ? "Categories" : "Contributions")
                            + "Not loaded properly. Please try again later");
        }
        receipt.setDonations(donations);
        receipt.setPaidSubscriptions(subscriptionsNames);
        member.setInstitution(institution);
        member.setReceipt(receipt);

        List<String> returnedMessage = new ArrayList<>();
        voicer.getCommunicationWays()
                .parallelStream()
                .forEach(communicationWay -> {
                    switch (communicationWay) {
                        case SMS:
                            if(member.getPhoneCode()==null || member.getPhone()==null) {
                                returnedMessage.add("No Phone number.");
                            }
                            break;

                        case WHATSAPP:
                            if(member.getWhatsappNumber()==null) {
                                returnedMessage.add("No Whatsapp number.");
                            }
                            break;

                        case MAIL:
                            if (member.getEmail() == null) {
                                returnedMessage.add("No Email.");
                            }
                            break;
                    }
                });


        AtomicReference<ReceiptAcknowledgement> acknowledgement = new AtomicReference<>();;
        if(returnedMessage.isEmpty()) {
            voicer.getCommunicationWays()
                    .parallelStream()
                    .forEach(communicationWay -> {
                        AtomicReference<Notification> notification = new AtomicReference<>();
                        AtomicBoolean messageSent = new AtomicBoolean(false);
                        ReceiptAcknowledgement acknowledger = new ReceiptAcknowledgement();
                        switch (communicationWay) {
                            case RECEIPT:
                                acknowledger.setInstitutionId(institution.getId());
                                acknowledger.setReceiptId(receipt.getId());
                                acknowledger.setSenderId(loggedInUser.getId());
                                acknowledger.setCommunicationWay(CommunicationWay.RECEIPT);
                                acknowledger.setMessageSendingTime(DateUtils.getNowDateTime());
                                ReceiptAcknowledgement savedAcknowledger = this.appRepository
                                        .getReceiptAcknowledgementRepository()
                                        .save(acknowledger);
                                acknowledgement.set(savedAcknowledger);
                                returnedMessage.add("Request Completed");

                                notification.set(Notification
                                        .createNotification(loggedInUser,
                                                "Receipt: " + receipt.getReceiptNo() + " Print",
                                                loggedInUser.getUsername()+" printed a receipt",
                                                NotificationActionType.READ,
                                                institution.getId()));
                                break;


                            case SMS:
                                returnedMessage.add("Request in Process. You will be acknowledged once it is done.");
                                ReceiptAcknowledgement smsAcknowledger = new ReceiptAcknowledgement();
                                messageSent.set(false);
                                CompletableFuture.runAsync(() -> {
                                    String notificationMessage = "No SMS Receipt Template set.";
                                    try {
                                        Template template = this.appRepository.getTemplateRepository().findFirstByInstitutionIdAndTemplateStyleOrderByIdDesc(institution.getId(),
                                                TemplateStyle.SMS_RECEIPT);
                                        if (template != null && !StringUtils.isEmpty(template.getSmsText())) {
                                            String receiptMessage = template.getSmsText();

                                            notificationMessage = "No Phone number is Set to send sms.";
                                            if (member.getPhone() != null) {
                                                receiptMessage = Messenger.getMessage(receiptMessage, member);
                                                receiptMessage = Messenger.formatToSMS(receiptMessage);
                                                List<String> numbers = Collections.singletonList(member.getPhone().toString());
                                                SmsSendRequest smsRequest = SmsSendRequest.builder()
                                                        .institutionId(institution.getId())
                                                        .numbers(numbers)
                                                        .directSend(false)
                                                        .message(receiptMessage)
                                                        .build();
                                                boolean smsSent = this.smsService.sendMessage(smsRequest);
                                                messageSent.set(smsSent);
                                                notificationMessage = "Failed to send SMS Message to \"" + member.getFullName() + "\"";
                                                if (messageSent.get()) {
                                                    smsAcknowledger.setInstitutionId(institution.getId());
                                                    smsAcknowledger.setReceiptId(receipt.getId());
                                                    smsAcknowledger.setSenderId(loggedInUser.getId());
                                                    smsAcknowledger.setMessageSendingTime(DateUtils.getNowDateTime());
                                                    this.appRepository.getReceiptAcknowledgementRepository()
                                                            .save(smsAcknowledger);
                                                    notificationMessage = "SMS Message sent to \"" + member.getFullName() + "\"";
                                                }
                                            }
                                        }
                                    } catch(Exception e){
                                        notificationMessage = "We have failed to send SMS receipt of \""+member.getReceipt().getReceiptNo()+"\"";
                                        e.printStackTrace();
                                    }
                                    notification.set(Notification
                                            .createNotification(loggedInUser,
                                                    "Receipt: " + receipt.getReceiptNo() + " SMS",
                                                    notificationMessage,
                                                    messageSent.get() ? NotificationActionType.CREATE : NotificationActionType.DELETE,
                                                    institution.getId()));
                                });
                                break;

                            case WHATSAPP:
                                if (member.getWhatsappNumber() != null) {
                                    returnedMessage.add("Request in Process. You will be acknowledged once it is done.");
                                    CompletableFuture.runAsync(()->{
                                        Template template = this.appRepository.getTemplateRepository()
                                                .findFirstByInstitutionIdAndTemplateStyleOrderByIdDesc(member.getInstitutionId(),
                                                        TemplateStyle.WHATSAPP_RECEIPT);
                                        String notificationMessage = "No WhatsApp Receipt Template Found!";
                                        NotificationActionType notifType = NotificationActionType.DELETE;
                                        if(template!=null && !StringUtils.isEmpty(template.getWhatsappTemplate())){
                                            try {
                                                String language = WhatsappMessageConfig.getLanguage(template.getLanguage());
                                                WhatsappMessageConfig messageConfig = WhatsappMessageConfig.builder()
                                                        .appRepository(this.appRepository)
                                                        .whatsappService(this.whatsappService)
                                                        .messageTitle("Receipt " + member.getReceipt().getReceiptNo())
                                                        .templateStyle(TemplateStyle.WHATSAPP_RECEIPT)
                                                        .language(language)
                                                        .templateName(template.getWhatsappTemplate().trim())
                                                        .sender(loggedInUser)
                                                        .build();
                                                boolean sent = member.sendWhatsappMessage(messageConfig);
                                                notificationMessage = "Receipt \""+receipt.getReceiptNo()
                                                        +"\" "
                                                        + (sent ? "sent" : "not sent")
                                                        + " via Whatsapp.";
                                                if(sent) notifType = NotificationActionType.CREATE;
                                            } catch(Exception e){
                                                e.printStackTrace();
                                                notificationMessage = "Failed to send Whatsapp Message.";
                                            }
                                        }
                                        notification.set(Notification
                                                .createNotification(loggedInUser,
                                                        "Receipt: " + receipt.getReceiptNo() + " WhatsApp",
                                                        notificationMessage,
                                                        notifType,
                                                        institution.getId()));
                                    });
                                }
                                break;

                            case MAIL:
                                ReceiptAcknowledgement emailAcknowledger = new ReceiptAcknowledgement();
                                returnedMessage.add("Request in Process. You will be acknowledged once it is done.");

                                CompletableFuture.runAsync(()->{
                                    String notificationMessage = member.getFullName() + " does not have an email.";
                                    NotificationActionType notifType = NotificationActionType.UPDATE;
                                    if (member.getEmail() != null) {
                                        try {
                                            Template emailTemplate = this.appRepository.getTemplateRepository()
                                                    .findFirstByInstitutionIdAndTemplateStyleOrderByIdDesc(member.getInstitutionId(),
                                                            TemplateStyle.EMAIL_RECEIPT);
                                            if(emailTemplate==null){
                                                notifType = NotificationActionType.DELETE;
                                                notificationMessage = "No Receipt Template Found";
                                            } else {
                                                String message = Messenger.getMessage(emailTemplate.getEmailText(), member);
                                                message = receipt.getCommunicationMessage(message);
                                                message = Messenger.formatToEmail(message);

                                                EmailToSend emailToSend = new EmailToSend();
                                                emailToSend.setEmail(member.getEmail());
                                                emailToSend.setInstitution(institution);
                                                emailToSend.setSubject("Receipt: " + receipt.getReceiptNo());
                                                emailToSend.setBody(message);
                                                emailToSend.setLoggedInUser(loggedInUser);
                                                Boolean emailSent = this.emailService.sendEmail(emailToSend);

                                                if(emailSent!=null) {
                                                    if (emailSent) {
                                                        emailAcknowledger.setInstitutionId(institution.getId());
                                                        emailAcknowledger.setReceiptId(receipt.getId());
                                                        emailAcknowledger.setCommunicationWay(CommunicationWay.MAIL);
                                                        emailAcknowledger.setSenderId(loggedInUser.getId());
                                                        emailAcknowledger.setMessageSendingTime(DateUtils.getNowDateTime());
                                                        this.appRepository.getReceiptAcknowledgementRepository()
                                                                .save(emailAcknowledger);

                                                        notifType = NotificationActionType.CREATE;
                                                        notificationMessage = "Email sent";
                                                    } else {
                                                        notifType = NotificationActionType.DELETE;
                                                        notificationMessage = "Failed to send email to " + member.getEmail() + " for the receipt \"" + receipt.getReceiptNo() + "\".";
                                                        notificationMessage += "Your institution can not send emails.";
                                                    }
                                                } else {
                                                    notificationMessage = null;
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            notifType = NotificationActionType.DELETE;
                                            notificationMessage = "Failed to send Email";
                                        }
                                    }

                                    if(notificationMessage!=null) {
                                        notification.set(Notification
                                                .createNotification(loggedInUser,
                                                        "Receipt: " + receipt.getReceiptNo() + " Mail",
                                                        notificationMessage,
                                                        notifType,
                                                        institution.getId()));
                                    }
                                });
                                break;
                        }

                        if (notification.get() != null) {
                            this.appRepository.getNotificationsRepository()
                                    .save(notification.get());
                        }
                    });
        }


        return ApiWorkFeedback.builder()
                .message(String.join(", ", returnedMessage))
                .successful(true)
                .object(acknowledgement.get())
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/upload-members")
    public ApiWorkFeedback uploadMembers(@RequestParam(name = "institutionId", required = false) Long institutionId,
                                         @RequestParam(name = "file") MultipartFile csvFile,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(institutionId==null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "No Institution Provided/Set.");
            }
            loggedInUser.setInstitutionId(institutionId);
        }

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Uploaded")
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR
    })
    @PostMapping("/search-member")
    public List<InstitutionMember> searchMembers(@Valid @RequestBody MemberSearchModel searchModel,
                                                 @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(searchModel, this.appRepository);

        return this.appRepository.getInstitutionMemberRepository()
                .searchUsingQuery(searchModel.getQuery(), searchModel.getInstitutionId())
                .stream()
                .filter(member->{
                    switch(searchModel.getCommunicationWay()){
                        case MAIL:
                            return !StringUtils.isEmpty(member.getEmail());

                        case SMS:
                            return member.getPhoneCode()!=null && member.getPhone()!=null;
                    }
                    return true;
                }).collect(Collectors.toList());
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/pincode/{code}")
    public ApiWorkFeedback getPincode(@PathVariable Integer code){
        Optional<PinCode> foundCodes = this.appRepository.getPincodeRepository().findByCode(code);
        String message = (!foundCodes.isPresent() ? "No such Pincode found" : "Pincode(s) found ");
        boolean success = (foundCodes.isPresent());
        return  ApiWorkFeedback
                .builder()
                .object(foundCodes)
                .successful(success)
                .message(message)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/list-marriage-birthdays")
    public PageVO getMarriageBirthdays(@RequestBody @Valid BirthdaysModel model,
                                       @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(model.getInstitutionId()).
                orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No such institution found");
                });

        return InstitutionFamily.getMarriages(this.getEntityManager(), institution, model);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/list-birthdays")
    public PageVO getBirthdays(@RequestBody @Valid BirthdaysModel model,
                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);

        Page<InstitutionMember> foundPage = InstitutionMember.getMembersBirthdays(this.getEntityManager(), model);

        CompletableFuture.runAsync(()->{
            SimpleDateFormat formatter = new SimpleDateFormat("MM dd");
            String content = formatter.format(model.getStart());
            if(model.getEnd()!=null) content += " - "+formatter.format(model.getEnd());
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    "Listed Birthdays",
                    content,
                    NotificationActionType.READ,
                    model.getInstitutionId()
            );
            this.appRepository.getNotificationsRepository().save(notification);
        });
        return PageVO.getPageVO(foundPage);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/recharge")
    public ApiWorkFeedback recharge(@Valid @RequestBody InstitutionTopUp recharge){
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(recharge.getInstitutionId())
                .orElse(null);
        if(institution!=null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "");
        }

        return null;
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-topup")
    public ApiWorkFeedback saveTopUp(@RequestBody @Valid TopUp topUp,
                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(topUp, this.appRepository);

        Institution savedInstitution = this.appRepository.getInstitutionRepository()
                .findById(topUp.getInstitutionId()).orElseThrow(()->{
                    // Show error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized.");
                });
        if(savedInstitution.isBlocked()){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    savedInstitution.getName()+ " is currently blocked.");
        }
        if(topUp.getEmail()!=null
                || topUp.getWhatsapp()!=null
                || topUp.getSms()!=null
                || topUp.getBackup()!=null
                || topUp.getAdditionalUser()!=null
                || topUp.getMembers()!=null
                || topUp.getFamilies()!=null
                || topUp.getChurchBranches()!=null){
            TopUp.saveTopUp(this.appRepository, topUp);
        } else {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "At least one value must be given.");
        }

        return ApiWorkFeedback
                .builder()
                .object(savedInstitution)
                .message("Saved Successful.")
                .build();
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR
    })
    @PostMapping("/set-ip")
    public ApiWorkFeedback setIpAddress(@RequestBody IpSetter ipSetter,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(ipSetter, this.appRepository);

        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(ipSetter.getInstitutionId())
                .orElseThrow(()->{
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Institution not recognized!");
                });
        if(StringUtils.isEmpty(ipSetter.getIp())) ipSetter.setIp(null);
        institution.setIpAddress(ipSetter.getIp());
        this.appRepository.getInstitutionRepository().save(institution);

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("IP Set")
                .build();
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR
    })
    @PostMapping("/get-ip")
    public ApiWorkFeedback getIpAddress(@RequestBody IpSetter ipSetter,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(ipSetter, this.appRepository);

        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(ipSetter.getInstitutionId())
                .orElseThrow(()->{
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Institution not recognized!");
                });

        return ApiWorkFeedback.builder()
                .successful(true)
                .object(institution.getIpAddress())
                .build();
    }
}

