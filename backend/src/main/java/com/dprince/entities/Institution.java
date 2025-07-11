package com.dprince.entities;

import com.dprince.apis.dashboard.vos.DashboardStats;
import com.dprince.apis.dashboard.vos.InstitutionTopper;
import com.dprince.apis.dashboard.vos.parts.CommunicationUsage;
import com.dprince.apis.institution.models.InstitutionListing;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.entities.enums.CertificateType;
import com.dprince.apis.institution.models.enums.DeadlineType;
import com.dprince.apis.institution.models.validations.*;
import com.dprince.apis.utils.AppStringUtils;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.models.enums.BlockSelector;
import com.dprince.configuration.ApplicationConfig;
import com.dprince.entities.enums.*;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.OverallInstitutionStats;
import com.dprince.startup.GeneralValues;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@FieldNameConstants
@Data
@Entity
@Table(name = "institutions",
        indexes = {
                @Index(name="index_institution_type", columnList = "institutionType"),
                @Index(name="index_subscription", columnList = "subscription"),
                @Index(name="index_subscription_plan", columnList = "subscriptionPlan"),
                @Index(name="index_category", columnList = "categoryId"),
                @Index(name="index_email", columnList = "email"),
                @Index(name="index_blocked", columnList = "blocked")
        })
public class Institution {
    private static final Logger LOGGER = LogManager.getLogger(Institution.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(nullable = false)
    private Long membersCount = 0L;
    public void incrementMembersCount(){
        this.membersCount++;
    }

    @JsonIgnore
    @Transient
    public String computeMemberCode(AppRepository appRepository){
        int i = 0, last = 20;
        for(; i<last; i++){
            Date today = DateUtils.getNowDateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            int year = calendar.get(Calendar.YEAR)%100,
                    month = calendar.get(Calendar.MONTH)+1;
            String monthString = (month<10) ? ("0"+month) : (month+"");
            long countString = 10000 + this.getMembersCount();
            this.incrementMembersCount();
            String theMemberCode = year + this.getBaseCode()+monthString+countString;
            InstitutionMember similarMember = appRepository.getInstitutionMemberRepository()
                    .findByCodeAndInstitutionId(theMemberCode, this.getId())
                    .orElse(null);
            if(similarMember==null) return theMemberCode;
        }
        return null;
    }
    @JsonIgnore
    @Transient
    private Long computedMemberCount(InstitutionMember lastInsertedMember){
        String memberCode = lastInsertedMember.getCode();
        // Remove the base code
        memberCode = memberCode.substring(2 + this.getBaseCode().length() + 2);
        return Long.parseLong(memberCode)-10000;
    }
    private void correctMembersCount(AppRepository appRepository){
        InstitutionMember lastInsertedMember = appRepository.getInstitutionMemberRepository()
                .findTopByInstitutionIdOrderByIdDesc(this.getId());
        Long members = lastInsertedMember==null
                ? 0
                : this.computedMemberCount(lastInsertedMember)+1;
        this.setMembersCount(members);
    }

    @JsonIgnore
    @Column(nullable = false)
    private Long donationsCount = 0L;

    public void correctDonationsCount(AppRepository appRepository){
        Long receipts = appRepository
                .getInstitutionReceiptsRepository()
                .countAllByInstitutionId(this.getId());
        this.setDonationsCount(receipts==null ? 0 : receipts);
    }

    public String generateReceiptNo(@NonNull AppRepository appRepository,
                                    @Nullable Long lastNumber){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR)%100,
                month = calendar.get(Calendar.MONTH)+1;
        String monthString = (month<10) ? "0"+month : month+"";

        long donationNumber = (lastNumber!=null)
                ? lastNumber+1
                : this.getDonationsCount()+10000;
        return this.getBaseCode()+"-"+year+"-"+monthString+"-"+donationNumber;
    }
    public Long retrieveDonationNumber(String receiptNo){
        if(StringUtils.isEmpty(receiptNo)) return 0L;
        String[] parts = receiptNo.split("-");
        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch(Exception ignored) {}
        return null;
    }

    public String generateFamilyCode(AppRepository appRepository){
        this.countFamilies(appRepository);
        return this.getBaseCode()+"-FAM-"+this.getFamilies();
    }

    public void autoCorrectData(AppRepository appRepository){
        this.correctMembersCount(appRepository);
        this.correctDonationsCount(appRepository);
        appRepository.getInstitutionRepository().save(this);
    }


    @Enumerated(EnumType.STRING)
    @NotNull(message = "Please specify the institution type.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    @Column(nullable = false, length = 10)
    private InstitutionType institutionType;

    @Transient
    public boolean isChurch(){
        return this.getInstitutionType().equals(InstitutionType.CHURCH);
    }

    private Long categoryId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private InstitutionCategory category;

    @NotNull(message = "The Subscription must be empty.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                InstitutionUpdateValidator.class
            })
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Subscription subscription;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<InstitutionCreditAccount> creditAccounts;
    private void populateAccounts(AppRepository appRepository){
        if(this.getId()!=null) {
            List<InstitutionCreditAccount> accounts = appRepository.getCreditAccountsRepository()
                    .findAllByInstitutionId(this.getId());
            this.setCreditAccounts(accounts);
        }
    }

    private String website;

    @JsonIgnore
    private long addressesPrint = 0;
    public void acknowledgeAddressPrinting(long count){
        this.addressesPrint += count;
    }
    @JsonIgnore
    private long receiptsGenerations = 0;
    public void incrementReceiptGenerations(){
        this.receiptsGenerations++;
    }
    @JsonIgnore
    private long reportsGenerations = 0;
    public void incrementReportsGenerations(){
        this.reportsGenerations += 1;
    }

    @NotNull(message = "Please provide the subscription plan.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                InstitutionUpdateValidator.class
            })
    private Long subscriptionPlan;
        public void fetchSubscriptionPlan(AppRepository appRepository){
            SubscriptionPlan localSubscription = GeneralValues.SUBSCRIPTIONS.get(this.getSubscriptionPlan());
            if(localSubscription==null){
                String message = "Failed to load "+this.getName()+" data";
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        message);
            }

            TopUp topUp = appRepository
                    .getTopUpRepository()
                    .findByInstitutionId(this.getId());
            if(topUp!=null) localSubscription.addTopUp(topUp);

            this.setInstitutionPlan(localSubscription);
        }

        @Transient
        public boolean canCreateFamily(AppRepository appRepository){
            Long families = appRepository.getInstitutionFamilyRepository()
                    .countAllByInstitutionId(this.getId());
            return this.getInstitutionPlan().getFamilies()>families;
        }

        @Transient
        public boolean canAddMembers(AppRepository appRepository){
            Long members = appRepository.getInstitutionMemberRepository()
                    .countAllByInstitutionId(this.getId());
            return this.getInstitutionPlan().getMembers()>members;
        }

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ChurchType churchType;
    private Long parentInstitutionId;
    @JsonIgnore
    @Transient
    private Institution parentInstitution;
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String parentInstitutionName;

    @NotBlank(message = "The Name must no be empty.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    private String name;
    public void setName(@NotBlank(message = "The Name must no be empty.",
            groups = {
                    ChurchCreationValidator.class,
                    OrganizationCreationValidator.class,
                    BranchChurchCreationValidator.class,
                    InstitutionUpdateValidator.class,
                    BranchUpdateValidator.class
            }) String name) {
        this.name = name.trim();
    }

    @Email(message = "Email is not valid.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    @NotBlank(message = "The Email must no be empty.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    @Column(nullable = false, length = 50)
    private String email;
    public void setEmail(@Email(message = "Email is not valid.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") @NotBlank(message = "The Email must no be empty.",
            groups = {
                    ChurchCreationValidator.class,
                    OrganizationCreationValidator.class,
                    BranchChurchCreationValidator.class,
                    InstitutionUpdateValidator.class,
                    BranchUpdateValidator.class
            }) String email) {
        this.email = StringUtils.isEmpty(email) ? null : email.trim();
    }

    @Column(length = 50)
    private String reportingEmail;


    @Min(value=1000000000, message="Phone number must be a positive number that has 10 digits.",
            groups = {
                ChurchCreationValidator.class
        })
    @NotNull(message = "Phone number must be specified.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    @Column(nullable = false)
    private Long phone;

    @NotNull(message = "Phone code must be specified.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    @Column(nullable = false)
    private Integer phoneCode;

    @NotBlank(message = "The Church address must no be empty.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class,
                BranchChurchCreationValidator.class,
                InstitutionUpdateValidator.class,
                BranchUpdateValidator.class
            })
    @Column(nullable = false)
    private String address;
    public void setAddress(@NotBlank(message = "The Church address must no be empty.",
            groups = {
                    ChurchCreationValidator.class,
                    OrganizationCreationValidator.class,
                    BranchChurchCreationValidator.class,
                    InstitutionUpdateValidator.class,
                    BranchUpdateValidator.class
            }) String address) {
        this.address = address.trim();
    }

    private String logo;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Map<CertificateType, String> certificates = new HashMap<>();
    public void populateCertificates(AppRepository appRepository){
        Map<CertificateType, String> certificates = appRepository
                .getCertificatesBackgroundsRepository()
                .findAllByInstitutionId(this.getId())
                .parallelStream()
                .collect(Collectors.toMap(CertificateBackground::getCertificateType,
                        CertificateBackground::getFileName));
        this.certificates = certificates;
    }



    @Column(nullable = false)
    private boolean blocked = false;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date creationDate = DateUtils.getNowDateTime();

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date deadline = DateUtils.getNowDateTime();
    public Date computeDeadline(Subscription subscription){
        Date deadline = this.getDeadline();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(deadline);
        switch (subscription){
            case ANNUAL:
                calendar.add(Calendar.YEAR, 1);
                break;

            case SEMESTRAL:
                calendar.add(Calendar.MONTH, 6);
                break;

            case QUARTER:
                calendar.add(Calendar.MONTH, 3);
                break;

            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;

            case WEEKLY:
                calendar.add(Calendar.DAY_OF_WEEK, 7);
                break;
        }
        return calendar.getTime();
    }

    @JsonIgnore
    @Transient
    public String getExpirationMessage(boolean isLogin){
        String expirationMessage = null;
        if(!this.getSubscription().equals(Subscription.LIFETIME)) {
            Date today = DateUtils.getNowDateTime();
            // Get
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date oneMonthDate = calendar.getTime();
            if (this.getDeadline().before(oneMonthDate)) {
                LocalDate deadline = Instant.ofEpochMilli(this.getDeadline().getTime()).atZone(ZoneId.of("Asia/Calcutta")).toLocalDate(),
                        todayLocalDate = today.toInstant().atZone(ZoneId.of("Asia/Calcutta")).toLocalDate();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                Period period = Period.between(todayLocalDate, deadline);
                if (!isLogin) {
                    expirationMessage = "Your subscription plan is about to expire." +
                            " Please contact the Dnote to Renew your subscription. " +
                            "Your plan will expire in " + period.getDays() + " days on " + deadline.format(formatter) + ".";
                }
                if (period.getDays() < 0) {
                    expirationMessage = "Your subscription plan has expired." +
                            " Please contact the Dnote to Renew your subscription. " +
                            "Your plan expired " + Math.abs(period.getDays()) + " days ago on " + deadline.format(formatter) + ".";
                }
            }
        }
        return expirationMessage;
    }

    @Transient
    @JsonIgnore
    public String getInstitutionTypeName(){
        return this.getInstitutionType().equals(InstitutionType.CHURCH)
                ? (this.getParentInstitutionId()==null ? "Church" : "Church Branch")
                : "General Member";
    }

    @Transient
    @JsonIgnore
    public String getMemberTitleName(){
        return this.getInstitutionType().equals(InstitutionType.CHURCH)
                ? "Member"
                : "Partner";
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long members;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxMembers;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<AppFeature> allowedFeatures;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long admins;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxAdmins;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long families;
    public void countFamilies(AppRepository appRepository){
        long allFamilies = appRepository.getInstitutionFamilyRepository()
                .countAllByInstitutionId(this.getId());
        this.setFamilies(allFamilies);
    }

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxFamilies;

    @NotBlank(message = "Base code mut not be empty.",
            groups = {
                ChurchCreationValidator.class,
                OrganizationCreationValidator.class
            })
    @Column(nullable = false, length = 50, unique = true)
    private String baseCode;
    public void setBaseCode(@NotBlank(message = "Base code mut not be empty.",
            groups = {
                    ChurchCreationValidator.class,
                    OrganizationCreationValidator.class
            }) String baseCode) {
        this.baseCode = baseCode.toUpperCase();
    }

    @NotBlank(message = "Please specify the default password",
            groups = {
                ChurchCreationValidator.class
            })
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String defaultPassword;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long emails = 0L;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxEmails;
        public void computeMaxEmails(AppRepository appRepository) {
            this.attachSubscriptionPlan(appRepository);
            this.addTheTopUp(appRepository);
            if(this.getTheTopUp()!=null) this.setMaxEmails(this.getPlan().getEmails() + this.getTheTopUp().getEmail());
            else this.setMaxEmails(this.getPlan().getEmails());
        }

        @Transient
        @JsonIgnore
        private boolean canSendEmails(AppRepository appRepository){
            this.computeMaxEmails(appRepository);
            return this.getEmails()<this.getMaxEmails();
        }

        @Transient
        @JsonIgnore
        private TopUp theTopUp;
        private void addTheTopUp(AppRepository appRepository){
            if(this.getTheTopUp()==null) {
                TopUp topUp = appRepository.getTopUpRepository().findByInstitutionId(this.getId());
                this.setTheTopUp(topUp);
            }
        }

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long smses = 0L;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxSmes;
        public void computeSmes(AppRepository appRepository){
            this.attachSubscriptionPlan(appRepository);
            this.addTheTopUp(appRepository);
            if(this.getTheTopUp()!=null) this.setMaxSmes(this.getPlan().getSmses() + this.getTheTopUp().getSms());
            else this.setMaxSmes(this.getPlan().getSmses());
        }
        @Transient
        @JsonIgnore
        public boolean canSendSMS(AppRepository appRepository){
            this.computeSmes(appRepository);
            return this.getMaxSmes()>this.getSmses();
        }

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long whatsapp = 0L;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxWhatsapp;

        public void computeMaxWhatsapp(AppRepository appRepository) {
            this.attachSubscriptionPlan(appRepository);
            this.addTheTopUp(appRepository);
            if(this.getTheTopUp()!=null) this.setMaxWhatsapp(this.getPlan().getWhatsapp() + this.getTheTopUp().getWhatsapp());
            else this.setMaxWhatsapp(this.getPlan().getWhatsapp());
        }

        @Transient
        @JsonIgnore
        private boolean canSendWhatsapp(AppRepository appRepository){
            this.computeMaxWhatsapp(appRepository);
            return this.getWhatsapp()<this.getMaxWhatsapp();
        }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Institution> branches;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long churchBranches;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long maxChurchBranches;

        @Transient
        @JsonIgnore
        public boolean canCreateBranch(SubscriptionPlan subscriptionPlan){
            if(subscriptionPlan.getChurchBranches()==null) return false;
            Long currentBranches = this.getChurchBranches()==null
                    ? 0
                    : this.getChurchBranches();
            return subscriptionPlan.getChurchBranches()-currentBranches>0;
        }
        public void updateChurchBranches(boolean creation){
            this.churchBranches = creation
                    ? this.churchBranches+1
                    : this.churchBranches-1;
        }

    public void computeSubscriptionDeadline(){
        if(this.getParentInstitutionId()==null) {
            if (!this.getSubscription().equals(Subscription.LIFETIME)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(this.getCreationDate());
                switch (this.getSubscription()) {
                    case ANNUAL:
                        calendar.add(Calendar.YEAR, 1);
                        break;

                    case SEMESTRAL:
                        calendar.add(Calendar.MONTH, 6);
                        break;

                    case QUARTER:
                        calendar.add(Calendar.MONTH, 3);
                        break;

                    case MONTHLY:
                        calendar.add(Calendar.MONTH, 1);
                        break;

                    case WEEKLY:
                        calendar.add(Calendar.DAY_OF_WEEK, 7);
                        break;
                }
                this.setDeadline(calendar.getTime());
            }
        }
    }

    public void adjustSubscriptionPlan(AppRepository appRepository){
        if(this.getParentInstitutionId()==null) {
            SubscriptionPlan plan = GeneralValues.SUBSCRIPTIONS.get(this.getSubscriptionPlan());
            if (plan == null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Subscription Plan not defined.");
            }
            this.setSmses(0L);
            this.setWhatsapp(0L);
            this.setEmails(0L);
        }
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SubscriptionPlan plan;
    public void attachSubscriptionPlan(AppRepository appRepository){
        if(this.getPlan()==null) {
            SubscriptionPlan plan = GeneralValues.SUBSCRIPTIONS.get(this.getSubscriptionPlan());
            if (plan == null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Failed to load Subscription plans.");
            }
            this.setPlan(plan);
        }
    }

    @Enumerated(EnumType.STRING)
    private ReceiptTemplate receiptTemplate = ReceiptTemplate.TEMPLATE_2;

    private String bibleVerse;

    private String receiptMessage;

    private Long receiptPhone;

    private String upi;

    private String customReceiptTemplate;
    private ReceiptTemplate customModel;

    public void adjust(AppRepository appRepository){
        // Search for category of this institution
        if(this.getCategoryId()!=null) {
            InstitutionCategory category = appRepository.getCategoryRepository()
                    .findById(this.getCategoryId())
                    .orElse(null);
            this.setCategory(category);
        }

        // Number of members
        long members = appRepository.getInstitutionMemberRepository()
                .countAllByInstitutionId(this.getId());
        this.setMembers(members);

        // Number of members
        long admins = appRepository.getUsersRepository()
                .countAllByInstitutionId(this.getId());
        this.setAdmins(admins);

        if(this.getInstitutionType().equals(InstitutionType.CHURCH)){
            // Rondera le nombre de famille
            long families = appRepository.getInstitutionFamilyRepository()
                    .countAllByInstitutionId(this.getId());
            this.setFamilies(families);
        }

        this.attachSubscriptionPlan(appRepository);
    }


    /**
     *
     * @param entityManager:
     *                     <p>
     *                          Receives the EntityManager object,
     *                          to handle complex SQL communication from your code.
     *                     </p>
     * @param appRepository:
     *                     <p>
     *                          This code may need AppRepository object. Make sure
     *                      it is provided so that other non-complex SQL query are handled with ease.
     *                     </p>
     * @param listQuery :
     *                  <p>
     *                      An InstitutionListing instance that contains the configuration of the search.
     *                  </p>
     * @return Page<Institution>
     */
    public static Page<Institution> makeSearch(EntityManager entityManager,
                      AppRepository appRepository,
                      InstitutionListing listQuery){
        Page<Institution> foundResults;
        try {
            String sqlString = "SELECT i FROM Institution i",
                    countString = "SELECT COUNT(i) FROM Institution i",
                    conditionString = " WHERE i.institutionType=:institutionType";

            if (listQuery.getInstitutionId() != null) {
                conditionString += " AND i.parentInstitutionId=:parentInstitutionId";
            } else {
                conditionString += " AND i.parentInstitutionId IS NULL";
            }

            if (!AppStringUtils.isEmpty(listQuery.getQuery())) {
                conditionString += " AND (LOWER(i.name) LIKE LOWER(:institutionName) OR LOWER(i.email) LIKE LOWER(:institutionName) OR LOWER(i.baseCode) LIKE LOWER(:institutionName))";
            }

            if(listQuery.getPhone()!=null){
                conditionString += " AND CONCAT(CONVERT(VARCHAR, i.phoneCode), CONVERT(VARCHAR, i.phone))=CONVERT(VARCHAR, :phone)";
            }

            if (!listQuery.getBlockSelector().equals(BlockSelector.ALL)) {
                conditionString += " AND i.blocked=:blockStatus";
            }

            if (listQuery.getFrom() != null) conditionString += " AND CONVERT(DATE, i.creationDate)>=CONVERT(DATE, :fromDate)";
            if (listQuery.getTo() != null) conditionString += " AND CONVERT(DATE, i.creationDate)<=CONVERT(DATE, :toDate)";

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtils.getNowDateTime());
            Date deadlineDate = null,
                    notDeadlineDate = null;
            switch (listQuery.getDeadlineType()) {
                case EXPIRING_SOON:
                case EXPIRED:
                    if (listQuery.getDeadlineType().equals(DeadlineType.EXPIRING_SOON)) {
                        calendar.add(Calendar.MONTH, 1);
                    }
                    deadlineDate = calendar.getTime();
                    conditionString += " AND CONVERT(DATE, i.deadline)<=CONVERT(DATE, :deadlineDate)";
                    break;

                case NOT_EXPIRING:
                    calendar.add(Calendar.MONTH, 1);
                    notDeadlineDate = calendar.getTime();
                    conditionString += " AND CONVERT(DATE, i.deadline)>=CONVERT(DATE, :notDeadlineDate)";
                    break;
            }

            // Query Execution
            TypedQuery<Institution> query = entityManager.createQuery(sqlString + conditionString, Institution.class);
            TypedQuery<Long> countQuery = entityManager.createQuery(countString + conditionString, Long.class);
            query.setParameter("institutionType", listQuery.getInstitutionType());
            countQuery.setParameter("institutionType", listQuery.getInstitutionType());

            if (listQuery.getInstitutionId() != null) {
                query.setParameter("parentInstitutionId", listQuery.getInstitutionId());
                countQuery.setParameter("parentInstitutionId", listQuery.getInstitutionId());
            }

            if (!listQuery.getBlockSelector().equals(BlockSelector.ALL)) {
                boolean blockedStatus = listQuery.getBlockSelector().equals(BlockSelector.BLOCKED);
                query.setParameter("blockStatus", blockedStatus);
                countQuery.setParameter("blockStatus", blockedStatus);
            }

            if (!AppStringUtils.isEmpty(listQuery.getQuery())) {
                query.setParameter("institutionName", "%" + listQuery.getQuery() + "%");
                countQuery.setParameter("institutionName", "%" + listQuery.getQuery() + "%");
            }

            if (listQuery.getPhone()!=null) {
                query.setParameter("phone", listQuery.getPhone());
                countQuery.setParameter("phone", listQuery.getPhone());
            }


            if (listQuery.getFrom() != null) {
                query.setParameter("fromDate", listQuery.getFrom());
                countQuery.setParameter("fromDate", listQuery.getFrom());
            }

            if (listQuery.getTo() != null) {
                query.setParameter("toDate", listQuery.getTo());
                countQuery.setParameter("toDate", listQuery.getTo());
            }

            if (deadlineDate != null) {
                query.setParameter("deadlineDate", deadlineDate);
                countQuery.setParameter("deadlineDate", deadlineDate);
            }
            if (notDeadlineDate != null) {
                query.setParameter("notDeadlineDate", notDeadlineDate);
                countQuery.setParameter("notDeadlineDate", notDeadlineDate);
            }

            // Set pagination parameters
            int page = listQuery.getPage() - 1;
            if (page < 0) page = 0;
            query.setFirstResult(page * listQuery.getSize());
            query.setMaxResults(listQuery.getSize());

            //count overall
            Long totalCount = 0L;
            try {
                totalCount = countQuery.getSingleResult();
                if (totalCount == null) totalCount = 0L;
            } catch (Exception ignored) {
            }

            List<Institution> resultList = query.getResultList();
            resultList.parallelStream().forEach(singleInstitution -> singleInstitution.populate(appRepository));
            foundResults = new PageImpl<>(resultList, PageRequest.of(page, listQuery.getSize()), totalCount);
        } catch(Exception e){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to perform the search");
        } finally {
            entityManager.close();
        }
        return foundResults;
    }



    private void populatePlansData(AppRepository appRepository){
        SubscriptionPlan plan = GeneralValues.SUBSCRIPTIONS.get(this.getSubscriptionPlan());
        if(plan!=null){
            this.setInstitutionPlan(plan);
            this.setMaxEmails(plan.getEmails());
            this.setMaxMembers(plan.getMembers());
            this.setMaxWhatsapp(plan.getWhatsapp());
            this.setMaxAdmins(plan.getAdmins());
            this.setMaxSmes(plan.getSmses());
            this.setMaxChurchBranches(plan.getChurchBranches());
        }
    }
    public void populate(AppRepository appRepository){
        this.adjust(appRepository);
        this.populatePlansData(appRepository);
        this.populateAccounts(appRepository);
        this.fetchSubscriptionPlan(appRepository);
        if(this.getParentInstitutionId()!=null){
            appRepository.getInstitutionRepository()
                    .findById(this.getParentInstitutionId())
                    .ifPresent(parentInstitution->{
                        this.setParentInstitution(parentInstitution);
                        this.setParentInstitutionName(parentInstitution.getName());
                    });
        } else {
            if(this.getInstitutionType().equals(InstitutionType.CHURCH)){
                if(this.getId()!=null) {
                    List<Institution> branches = appRepository.getInstitutionRepository()
                            .findAllByParentInstitutionId(this.getId());
                    if(!branches.isEmpty()){
                        branches.parallelStream().forEach(singleBranch-> {
                            singleBranch.adjust(appRepository);
                            singleBranch.setCreditAccounts(this.getCreditAccounts());
                            singleBranch.setSubscriptionPlan(this.getSubscriptionPlan());
                        });
                        this.setBranches(branches);
                    }
                }
            }
        }
    }

    @Transient
    @JsonIgnore
    public String getToggleBlockMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div>");
        stringBuilder.append("<div>Dear ").append(this.getInstitutionTypeName()).append(", </div>");
        stringBuilder.append("<p>We are writing to inform you that your account on ").append(ApplicationConfig.APPLICATION_NAME + " ")
                .append((this.blocked) ? "has been  Blocked." : " has been unblocked")
                .append((this.blocked) ? " During this suspension period, you will not be able to access your " +
                        "account or any of the services provided by "
                        : " and is now fully accessible. "
                )
                .append((this.blocked) ? ApplicationConfig.APPLICATION_NAME : "")
                .append(" </p>");
        stringBuilder.append("<p>")
                .append("Best regards,")
                .append("<div style='font-weight:bold;'>" + ApplicationConfig.APPLICATION_NAME + " Team</div>")
                .append("</p>");

        stringBuilder.append("</div>");
        return stringBuilder.toString();
    }


    @Transient
    private SubscriptionPlan institutionPlan;

    @JsonIgnore
    @Transient
    public boolean canCommunicate(AppRepository appRepository,
                                  CommunicationWay communicationWay){
        Institution institution = this;
        if(this.getParentInstitutionId()!=null){
            institution = appRepository.getInstitutionRepository()
                    .findById(institution.getParentInstitutionId())
                    .orElse(null);
            if (institution == null) return false;
        }
        switch(communicationWay){
            case SMS:
                return institution.canSendSMS(appRepository);

            case MAIL:
                return institution.canSendEmails(appRepository);

            case WHATSAPP:
                return institution.canSendWhatsapp(appRepository);
        }
        return false;
    }

    @JsonIgnore
    @Transient
    public static OverallInstitutionStats getStats(@NotNull EntityManager entityManager){
        OverallInstitutionStats stats;
        try {
            String sql = "SELECT new com.dprince.entities.vos.OverallInstitutionStats(" +
                    "SUM(ins.smses) as sms," +
                    "SUM(ins.whatsapp) as whatsapp," +
                    "SUM(ins.emails) as emails" +
                    ") FROM Institution ins";
            TypedQuery<OverallInstitutionStats> query = entityManager.createQuery(sql,
                    OverallInstitutionStats.class);
            stats = query.getSingleResult();
        } finally {
            entityManager.close();
        }
        return stats;
    }

    @JsonIgnore
    @Transient
    public void preDelete(@NonNull User loggedInUser,
                          @NonNull AppRepository appRepository,
                          @NonNull EmailService emailService){
        List<User> superAdmins = appRepository.getUsersRepository()
                .findAllByUserType(UserType.SUPER_ADMINISTRATOR);
        List<String> emails = superAdmins.parallelStream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        EmailToSend emailToSend = new EmailToSend();
        emailToSend.setEmails(emails);
        emailToSend.setSubject(this.getName()+": Deletion");

        String deletionToken = AppStringUtils.generateRandomString(10);
        this.setDeletionToken(deletionToken);
        Date deadline = DateUtils.addDays(1);
        this.setDeletionTokenDeadline(deadline);

        String email = "<div>Dear</div>";
        email += "<div>" +
                "Please confirm you are deleting this institution("+this.getName()+") " +
                "by <a href=\""+ApplicationConfig.APP_URL+"/institution/confirm-deletion/"+deletionToken+"\" title='Click Here to Delete'>clicking here</a>" +
                "</div>" +
                "<div>This action is being carried out by <strong>"+loggedInUser.getFullName()+"</strong></div>";
        emailToSend.setEmail(email);
        emailToSend.setSystemMail(true);
        emailService.sendEmail(emailToSend);
    }

    @JsonIgnore
    @Transient
    public void delete(AppRepository appRepository){
        final Institution institution = this;
        appRepository.getInstitutionRepository().deleteById(this.getId());
        CompletableFuture.runAsync(()->{
            if(this.getParentInstitutionId()!=null){
                // Update the number of branches;
                appRepository
                        .getInstitutionRepository()
                        .findById(this.getParentInstitutionId())
                        .ifPresent(parentInstitution->{
                            parentInstitution.updateChurchBranches(false);
                            appRepository.getInstitutionRepository().save(parentInstitution);
                        });
            }
            // Delete all the users
            appRepository.getUsersRepository().deleteAllByInstitutionId(institution.getId());
            appRepository.getInstitutionMemberRepository().deleteAllByInstitutionId(institution.getId());
            if(institution.getInstitutionType().equals(InstitutionType.CHURCH)) {
                appRepository.getChurchContributionRepository().deleteAllByInstitutionId(institution.getId());
                appRepository.getChurchEventRepository().deleteAllByInstitutionId(institution.getId());

                List<Long> groupIds = appRepository.getGroupRepository()
                        .findAllByInstitutionId(institution.getId())
                        .stream().map(InstitutionGroup::getId)
                        .collect(Collectors.toList());
                appRepository.getGroupRepository().deleteAllByInstitutionId(institution.getId());
                appRepository.getGroupMemberRepository().deleteAllByGroupIdIn(groupIds);
                appRepository.getInstitutionDonationRepository().deleteAllByInstitutionId(institution.getId());
                appRepository.getPriestSignatureRepository().deleteAllByInstitutionId(institution.getId());
            }
            appRepository.getCommunicationRepository().deleteAllByInstitutionId(institution.getId());
            appRepository.getInstitutionFamilyRepository().deleteAllByInstitutionId(institution.getId());
            appRepository.getInstitutionFamilyMemberRepository().deleteAllByInstitutionId(institution.getId());
            appRepository.getInstitutionReceiptsRepository().deleteAllByInstitutionId(institution.getId());

            // Do the same for all the children
            if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
                appRepository.getInstitutionRepository()
                        .findAllByParentInstitutionId(institution.getId())
                        .parallelStream()
                        .forEach(branch-> branch.delete(appRepository));
            }
        });
    }


    @Transient
    @JsonIgnore
    public Map<String, Long> getPartnerWiseStats(AppRepository appRepository){
        Map<String, Long> outputMap = new HashMap<>();
        List<InstitutionCategory> categories = appRepository.getCategoryRepository()
                .findAllByInstitutionId(this.getId());

        if(this.getInstitutionType().equals(InstitutionType.GENERAL)) {
            categories.parallelStream()
                    .forEach(category -> {
                        Long peopleInCategory = appRepository.getInstitutionMemberRepository()
                                .countAllCategoryWise(category.getId().toString());
                        outputMap.put(category.getName(), peopleInCategory);
                    });
        }

        return outputMap;
    }



    @Transient
    @JsonProperty
    public static DashboardStats getGeneralStats(@Nullable Institution institutionInUse,
                                                 @NonNull EntityManager entityManager,
                                                 @NonNull AppRepository appRepository,
                                                 @NonNull DashboardStats stats){
        try {
            if (institutionInUse == null) {
                List<Institution> allInstitutions = appRepository.getInstitutionRepository().findAll();
                AtomicLong churches = new AtomicLong(0),
                        organizations = new AtomicLong(0),
                        branches = new AtomicLong(0),

                        blockedChurches = new AtomicLong(0),
                        blockedOrganizations = new AtomicLong(0);

                List<Long> churchesIds = new ArrayList<>(),
                        organizationsIds = new ArrayList<>();
                allInstitutions.parallelStream().forEach(institution -> {
                    if (institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                        organizations.getAndIncrement();
                        organizationsIds.add(institution.getId());
                        if (institution.isBlocked()) blockedOrganizations.getAndIncrement();
                    } else {
                        churchesIds.add(institution.getId());
                        if (institution.isBlocked()) blockedChurches.getAndIncrement();
                        if (institution.getParentInstitutionId() == null) churches.getAndIncrement();
                        else branches.getAndIncrement();
                    }
                });

                stats.setChurches(churches.get());
                stats.setChurchBranches(branches.get());
                stats.setOrganizations(organizations.get());

                stats.setBlockedChurches(blockedChurches.get());
                stats.setBlockedOrganizations(blockedOrganizations.get());


                // Church
                long foundChurchAdmins = appRepository.getUsersRepository()
                        .countAllByUserTypeAndInstitutionIdIn(UserType.CHURCH_ADMINISTRATOR, churchesIds),

                        foundChurchAssistantAdmins = appRepository.getUsersRepository()
                                .countAllByUserTypeAndInstitutionIdIn(UserType.CHURCH_ASSISTANT_ADMINISTRATOR, churchesIds),

                        foundOrganizationAdmins = appRepository.getUsersRepository()
                                .countAllByUserTypeAndInstitutionIdIn(UserType.ORGANIZATION_ADMINISTRATOR, organizationsIds),

                        foundOrganizationAssistantAdmins = appRepository.getUsersRepository()
                                .countAllByUserTypeAndInstitutionIdIn(UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR, organizationsIds);

                stats.setChurchAdmins(foundChurchAdmins);
                stats.setChurchAssistantAdmins(foundChurchAssistantAdmins);

                stats.setOrganizationAdmins(foundOrganizationAdmins);
                stats.setOrganizationAssistantAdmins(foundOrganizationAssistantAdmins);


                // Families
                long churchFamilies = InstitutionFamily.
                        countFamiliesInInstitutionType(entityManager, InstitutionType.CHURCH),

                        organizationFamilies = InstitutionFamily.
                                countFamiliesInInstitutionType(entityManager, InstitutionType.GENERAL);

                stats.setChurchFamilies(churchFamilies);
                stats.setOrganizationFamilies(organizationFamilies);
            } else {
                long families = appRepository.getInstitutionFamilyRepository()
                        .countAllByInstitutionId(institutionInUse.getId());
                stats.setFamilies(families);
            }


            if (institutionInUse != null) {
                CommunicationUsage communicationUsage = Institution
                        .getCommunicationUsage(
                                institutionInUse,
                                null,
                                entityManager);
                stats.setUsedWhatsapp(communicationUsage.getUsedWhatsapp());
                stats.setUsedEmails(communicationUsage.getUsedEmails());
                stats.setUsedSms(communicationUsage.getUsedSms());
            } else {
                CommunicationUsage churchCommunicationUsage = Institution
                        .getCommunicationUsage(
                                null,
                                InstitutionType.CHURCH,
                                entityManager);
                stats.setChurchUsedWhatsapp(churchCommunicationUsage.getUsedWhatsapp());
                stats.setChurchUsedEmails(churchCommunicationUsage.getUsedEmails());
                stats.setChurchUsedSms(churchCommunicationUsage.getUsedSms());

                CommunicationUsage organizationCommunicationUsage = Institution
                        .getCommunicationUsage(
                                null,
                                InstitutionType.GENERAL,
                                entityManager);
                stats.setOrganizationUsedWhatsapp(organizationCommunicationUsage.getUsedWhatsapp());
                stats.setOrganizationUsedEmails(organizationCommunicationUsage.getUsedEmails());
                stats.setOrganizationUsedSms(organizationCommunicationUsage.getUsedSms());
            }
        } finally {
            entityManager.close();
        }
        return stats;
    }

    @Transient
    @JsonIgnore
    public static CommunicationUsage getCommunicationUsage(@Nullable Institution institution,
                                                           @Nullable InstitutionType institutionType,
                                                           @NonNull EntityManager entityManager){
        String sql = "SELECT new com.dprince.apis.dashboard.vos.parts.CommunicationUsage(" +
                "SUM(i.smses) as usedSms," +
                "SUM(i.emails) as usedEmails," +
                "SUM(i.whatsapp) as usedWhatsapp" +
                ")" +
                " FROM Institution i";
        if (institution != null) sql += " WHERE i.id=:institutionId";
        else sql += " WHERE i.institutionType=:institutionType";
        TypedQuery<CommunicationUsage> query = entityManager.createQuery(sql, CommunicationUsage.class);
        if (institution != null) query.setParameter("institutionId", institution.getId());
        else query.setParameter("institutionType", institutionType);
        return query.getSingleResult();
    }



    @Transient
    @JsonIgnore
    public static void getToppersStats(@NonNull EntityManager entityManager,
                                       InstitutionType institutionType,
                                       @NonNull DashboardStats dashboardStats){
        try {
            String sql = "SELECT new com.dprince.apis.dashboard.vos.InstitutionTopper(" +
                    "i.name as name," +
                    "(SELECT COUNT(*) FROM InstitutionMember im WHERE im.institutionId=i.id) as members," +
                    "i.receiptsGenerations as receipts," +
                    "i.addressesPrint as addresses" +
                    ") FROM Institution i" +
                    " WHERE i.institutionType=:institutionType";
            TypedQuery<InstitutionTopper> query = entityManager
                    .createQuery(sql, InstitutionTopper.class);
            query.setParameter("institutionType", institutionType);
            query.setMaxResults(5);
            List<InstitutionTopper> resultList = query.getResultList();
            resultList.sort(Comparator.comparing(
                            InstitutionTopper::getMembers).reversed()
                    .thenComparing(InstitutionTopper::getReceipts).reversed()
                    .thenComparing(InstitutionTopper::getAddresses).reversed());
            int totalInstitution = resultList.size();
            List<InstitutionTopper> toppers = resultList.subList(0, Math.min(totalInstitution, 5));
            if (institutionType.equals(InstitutionType.GENERAL)) {
                dashboardStats.setOrganizationToppers(toppers);
            } else {
                dashboardStats.setChurchToppers(toppers);
            }
        } finally {
            entityManager.close();
        }
    }

    @Column(length = Integer.MAX_VALUE)
    private String priestSignature;

    @JsonIgnore
    @Column(length = 20)
    private String ipAddress;

    @JsonIgnore
    @Column(length=12)
    private String deletionToken;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletionTokenDeadline;
}
