package com.dprince.entities;

import com.dprince.apis.institution.models.AdminListingQuery;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.configuration.ApplicationConfig;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.utils.DbTable;
import com.dprince.entities.vos.PageVO;
import com.dprince.security.UpdatableBCrypt;
import com.dprince.security.jwt.JwtTokenUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@FieldNameConstants
@Service("userService")
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="users", indexes = {
       @Index(name = "idx_username", columnList = "username"),
       @Index(name = "idx_email", columnList = "email"),
       @Index(name = "idx_phone", columnList = "phone")
})
public class User extends DbTable implements UserDetails, UserDetailsService {
    @Autowired
    @JsonIgnore
    @Transient
    private JwtTokenUtil jwtTokenUtil;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private UserType userType;

    @JsonIgnore
    @Transient
    public static List<UserType> getUserTypesExcept(List<UserType> userTypes){
        return Arrays.stream(UserType.values())
                .filter(singleUserType-> !userTypes.contains(singleUserType))
                .collect(Collectors.toList());
    }

    @Column(unique=true, nullable = false, length = 60)
    private String username;

    @Column(unique=true, nullable = false, length = 60)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 120)
    private String password;

    @Column(nullable=false, length = 30)
    private String firstName;

    @Column(nullable=false, length = 40)
    private String lastName;
    @Transient
    @JsonIgnore
    public String getFullName(){
        String fullName = "";
        if(!StringUtils.isEmpty(this.getFirstName())) fullName += this.getFirstName().toUpperCase();
        if(!StringUtils.isEmpty(this.getLastName())){
            fullName += ((!StringUtils.isEmpty(username)) ? " " : "") + this.getLastName().toUpperCase();
        }
        return fullName;
    }

    @Column(nullable = false, unique = true)
    public Long phone = null;

    @Column(nullable = false, length = 50)
    public String staffId;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    public Gender gender = Gender.NOT_DEFINED;

    // Must be an institutionType
    public Long institutionId;
        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Institution institution;

    // Default constructor
    public User() {
        // No initialization needed, fields are already set to null
    }


    @JsonIgnore
    @Transient
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }


    @JsonIgnore
    @Transient
    private boolean accountNonExpired = true;
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @JsonIgnore
    @Transient
    private boolean accountNonLocked = true;
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @JsonIgnore
    @Transient
    private boolean credentialsNonExpired = true;
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @JsonIgnore
    @Transient
    private boolean enabled = true;
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.getRepository().getUsersRepository().findByUsername(username);
    }

    public UserDetails loadClientFromRequest(String username, String authToken, HttpServletRequest request){
        UserDetails userDetails = this.loadUserByUsername(username);
        if (jwtTokenUtil.validateToken(authToken, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            Collections.singletonList(new SimpleGrantedAuthority("USER")));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return userDetails;
    }


    @Transient
    @JsonIgnore
    public static User getUserFromSession(HttpSession session){
        Object sessionObject = session.getAttribute("loggedInUser");
        if(sessionObject==null) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                "User not found.");
        return (User) sessionObject;
    }

    @Transient
    @JsonIgnore
    public List<UserType> getAllowedToCreateUserTypes(){
        List<UserType> output = new ArrayList<>();
        switch(this.getUserType()){
            case SUPER_ADMINISTRATOR:
                output.add(UserType.SUPER_ADMINISTRATOR);
                output.add(UserType.SUPER_ASSISTANT_ADMINISTRATOR);
                break;

            case CHURCH_ADMINISTRATOR:
                output.add(UserType.CHURCH_ASSISTANT_ADMINISTRATOR);
                output.add(UserType.CHURCH_MEMBER);
                break;

            case ORGANIZATION_ADMINISTRATOR:
                output.add(UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR);
                output.add(UserType.ORGANIZATION_PARTNER);
                break;
        }
        return output;
    }

    @Transient
    @JsonIgnore
    public static List<UserType> noOrganizationUsers(){
        List<UserType> output = new ArrayList<>();
        output.add(UserType.SUPER_ADMINISTRATOR);
        output.add(UserType.SUPER_ASSISTANT_ADMINISTRATOR);
        return output;
    }

    @Transient
    @JsonIgnore
    public static List<UserType> getMainChurchAdministrators(){
        List<UserType> output = new ArrayList<>();
        output.add(UserType.CHURCH_ADMINISTRATOR);
        output.add(UserType.CHURCH_ASSISTANT_ADMINISTRATOR);
        return output;
    }

    @Transient
    @JsonIgnore
    public void patchInstitutionIdInUse(@NonNull Institutionable institutionable,
                                        @NonNull AppRepository appRepository){
        if(User.getMainChurchAdministrators().contains(this.getUserType())){
            if(institutionable.getInstitutionId()!=null) {
                if(institutionable.getInstitutionId().equals(this.getInstitutionId())){
                    institutionable.setInstitutionId(this.getInstitutionId());
                } else {
                    // isigura ko ariko akoresha branch
                    Institution branch = appRepository.getInstitutionRepository()
                            .findByParentInstitutionIdAndId(this.getInstitutionId(),
                                    institutionable.getInstitutionId())
                            .orElseThrow(() -> {
                                // Ariko agerageza kw'accessing ibitari ivyabo
                                return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                        "No such branch.");
                            });
                    institutionable.setInstitutionId(branch.getId());
                }
            } else {
                institutionable.setInstitutionId(this.getInstitutionId());
            }
        } else {
            institutionable.setInstitutionId(this.getInstitutionId());
        }
    }

    public static User generateUser(Institution institution){
        User user = new User();
        String password = UpdatableBCrypt.hash(institution.getDefaultPassword());
        user.setUsername(institution.getEmail());
        user.setStaffId(institution.getBaseCode()+"-0001");
        user.setPassword(password);
        user.setFirstName("Administrator");
        user.setLastName(institution.getName());
        if(institution.getParentInstitutionId()==null) {
            user.setUserType(institution.getInstitutionType().equals(InstitutionType.CHURCH)
                    ? UserType.CHURCH_ADMINISTRATOR
                    : UserType.ORGANIZATION_ADMINISTRATOR);
        } else {
            user.setUserType(UserType.CHURCH_BRANCH_ADMINISTRATOR);
        }
        user.setInstitutionId(institution.getId());
        user.setPhone(institution.getPhone());
        user.setEmail(institution.getEmail());
        user.setGender(Gender.NOT_DEFINED);
        return user;
    }

    @JsonIgnore
    @Transient
    public boolean withInstitution = true;

    @JsonIgnore
    @Transient
    private boolean performingUpdate;

    public void checkUserFulfillsRequirements(Institutionable institutionable,
                                              @NonNull AppRepository appRepository){
        if(User.noOrganizationUsers().contains(this.getUserType())){
            if(this.isWithInstitution()) {
                if (institutionable.getInstitutionId() == null) {
                    throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "No institution specified.");
                }
            }
        } else {
            if(!this.isPerformingUpdate()) {
                this.patchInstitutionIdInUse(institutionable, appRepository);
            }
        }
    }

    public String getWelcomeMessage(Institution institution){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div>");
            stringBuilder.append("<div>Dear ").append(this.getFirstName()).append(", </div>");
            stringBuilder.append("<p'>Welcome to "+ ApplicationConfig.APPLICATION_NAME +"! We are excited to have you join our community.</p>");
            stringBuilder.append("<p>Your account has been successfully created. Here are your login details:</p>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Application Login Link :</span>:<a href='"+ApplicationConfig.APP_URL+"' title='"+ApplicationConfig.APPLICATION_NAME+"'>").append(ApplicationConfig.APP_URL).append("</a></div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Organization Name :</span>:").append(institution.getName()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Organization Base Code </span>:").append(institution.getBaseCode()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Email Id </span>:").append(institution.getEmail()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Mobile Number </span>:").append(institution.getPhone()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Subscription Plan </span>:").append(institution.getInstitutionPlan().getName()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Emails </span>:").append(institution.getInstitutionPlan().getEmails()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Sms  </span>:").append(institution.getInstitutionPlan().getSmses()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Whatsapp </span>:").append(institution.getInstitutionPlan().getWhatsapp()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Members </span>:").append(institution.getInstitutionPlan().getMembers()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Admins </span>:").append(institution.getInstitutionPlan().getAdmins()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Families </span>:").append(institution.getInstitutionPlan().getFamilies()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>No.of.Branches </span>:").append(institution.getInstitutionPlan().getChurchBranches()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Subscription </span>:").append(institution.getSubscription()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Organization Address</span>:").append(institution.getAddress()).append("</div>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Username: </span><strong>").append(this.getUsername()).append("</strong></div>");
            stringBuilder.append("<div'><span style='font-weight: bold;'>Default Password: </span><strong>").append(institution.getDefaultPassword()).append("</strong></div>");

        stringBuilder.append("<p>")
                .append("For security reasons, we recommend changing your password as soon as possible. ")
                .append("You can do this by logging into your account and navigating to the ")
                .append("Account Settings section.")
                .append("</p>");

        stringBuilder.append("<p>")
                .append("If you have any questions or need assistance, ")
                .append("feel free to reach out to our support team at "+ApplicationConfig.SUPPORT_EMAIL+".")
                .append("</p>");

        stringBuilder.append("<p>")
                .append("Thank you for choosing "+ApplicationConfig.APPLICATION_NAME+". We look forward to serving you!")
                .append("</p>");


        stringBuilder.append("<p>")
                .append("Best regards,")
                .append("<div style='font-weight:bold;'>"+ApplicationConfig.APPLICATION_NAME+" Team</div>")
                .append("</p>");

        stringBuilder.append("</div>");

        return stringBuilder.toString();
    }

    public String getUserWelcomeMessage(String password){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div>");
            stringBuilder.append("<div>Dear ").append(this.getFirstName()).append(", </div>");
            stringBuilder.append("<p'>Welcome to "+ ApplicationConfig.APPLICATION_NAME +"! We are excited to have you join our community.</p>");
            stringBuilder.append("<p>Your account has been successfully created. Here are your login details:</p>");
            stringBuilder.append("<div><span style='font-weight: bold;'>Username: </span><strong>").append(this.getUsername()).append("</strong></div>");
            stringBuilder.append("<div'><span style='font-weight: bold;'>Default Password: </span><strong>").append(password).append("</strong></div>");

        stringBuilder.append("<p>")
                .append("For security reasons, we recommend changing your password as soon as possible. ")
                .append("You can do this by logging into your account and navigating to the ")
                .append("Account Settings section.")
                .append("</p>");

        stringBuilder.append("<p>")
                .append("If you have any questions or need assistance, ")
                .append("feel free to reach out to our support team at "+ApplicationConfig.SUPPORT_EMAIL+".")
                .append("</p>");

        stringBuilder.append("<p>")
                .append("Thank you for choosing "+ApplicationConfig.APPLICATION_NAME+". We look forward to serving you!")
                .append("</p>");


        stringBuilder.append("<p>")
                .append("Best regards,")
                .append("<div style='font-weight:bold;'>"+ApplicationConfig.APPLICATION_NAME+" Team</div>")
                .append("</p>");

        stringBuilder.append("</div>");

        return stringBuilder.toString();
    }

    public static PageVO listAdmins(EntityManager entityManager,
                                 AppRepository appRepository,
                                 AdminListingQuery adminListingQuery){
        String selectSql = "SELECT u FROM User u",
                whereQuery = " WHERE u.institutionId=:institutionId";
        if(!StringUtils.isEmpty(adminListingQuery.getQuery())){
            whereQuery += " AND (LOWER(u.firstName) LIKE LOWER(:query) OR LOWER(u.lastName) LIKE LOWER(:query) OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(:query) OR LOWER(u.username) LIKE (:query))";
        }

        whereQuery += " ORDER BY u.firstName, u.lastName";

        TypedQuery<User> selectQuery = entityManager.createQuery(selectSql+whereQuery, User.class);
        selectQuery.setParameter("institutionId", adminListingQuery.getInstitutionId());
        if(!StringUtils.isEmpty(adminListingQuery.getQuery())) {
            selectQuery.setParameter("query", "%"+adminListingQuery.getQuery()+"%");
        }
        List<User> results = selectQuery.getResultList();
        Page<User> resultsPage = new PageImpl<>(results,
                PageRequest.of(0, adminListingQuery.getSize()), results.size());
        return PageVO.getPageVO(resultsPage);
    }


}
