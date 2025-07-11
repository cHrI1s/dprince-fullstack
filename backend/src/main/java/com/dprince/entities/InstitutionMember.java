package com.dprince.entities;

import com.dprince.apis.dashboard.utils.DashboardUtils;
import com.dprince.apis.dashboard.utils.enums.DashboardDuration;
import com.dprince.apis.dashboard.vos.DashboardStats;
import com.dprince.apis.dashboard.vos.parts.GenderStat;
import com.dprince.apis.institution.models.AddressRequestModel;
import com.dprince.apis.institution.models.BirthdaysModel;
import com.dprince.apis.institution.models.MemberListingQuery;
import com.dprince.apis.institution.models.donations.DonationContribution;
import com.dprince.apis.institution.models.enums.DurationGap;
import com.dprince.apis.institution.models.validations.ChurchMemberCreationValidator;
import com.dprince.apis.institution.models.validations.PartnerCreationValidator;
import com.dprince.apis.utils.AppStringUtils;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.vos.DateGap;
import com.dprince.configuration.database.converters.SetOfLongConverter;
import com.dprince.configuration.whatsapp.models.WhatsappMessage;
import com.dprince.configuration.whatsapp.models.enums.WhatsappMessageType;
import com.dprince.configuration.whatsapp.models.part.WhatsappMessageContext;
import com.dprince.configuration.whatsapp.models.part.WhatsappMessageData;
import com.dprince.configuration.whatsapp.models.vos.WhatsappApiResponse;
import com.dprince.configuration.whatsapp.utils.WhatsappMessageConfig;
import com.dprince.entities.embeded.PersonAddress;
import com.dprince.entities.enums.*;
import com.dprince.entities.parts.SubscriptionData;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.MemberDTO;
import com.dprince.startup.GeneralValues;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period; 
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;


import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Collections; // Make sure this is imported
import java.util.TreeMap;     // Make sure this is imported
import java.util.LinkedHashMap;
import java.util.Locale;
import java.lang.StringBuilder;
import java.util.Comparator;

@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "institution_members",
        indexes = {
                @Index(name = "index_title_id", columnList = "titleId"),
                @Index(name = "index_institution_id", columnList = "institutionId"),
                @Index(name = "index_first_name", columnList = "firstName"),
                @Index(name = "index_last_name", columnList = "lastName"),
                @Index(name = "index_code", columnList = "code"),
                @Index(name = "index_country", columnList = "country"),
                @Index(name = "index_country_state", columnList = "state"),
                @Index(name = "index_phone", columnList = "phone"),
                @Index(name = "index_pincode", columnList = "pincode"),
                @Index(name = "index_id", columnList = "id")
        },
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"code", "institutionId"}), 
            @UniqueConstraint(columnNames = {"phoneCode", "phone"})
         
    })

public class InstitutionMember extends PersonAddress implements Institutionable {
    private static final Logger LOGGER = LogManager.getLogger(InstitutionMember.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    
   
    @Column(nullable = false)
    private Long institutionId;
        @JsonIgnore
        @Transient
        private Institution institution;

    @NotNull(message = "Please specify the title.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false)
    private Long titleId;


    @Transient
    private String title;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "First name must be provided.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false, length = 40)
    private String firstName;
    public void setFirstName(@NotBlank(message = "First name must be provided.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    }) String firstName) {
        this.firstName = firstName.trim();
    }

    @NotBlank(message = "Last name must be provided.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false, length = 40)
    private String lastName;
    public void setLastName(@NotBlank(message = "Last name must be provided.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    }) String lastName) {
        this.lastName = lastName.trim();
    }

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Transient
        private String fullName;
        public String getFullName(){
            String fullName = "";
            if(!StringUtils.isEmpty(this.getFirstName())) fullName += this.getFirstName();
            if(!StringUtils.isEmpty(this.getLastName())) {
                fullName += (!StringUtils.isEmpty(this.getFirstName()) ? " " : "") + this.getLastName();
            }
            return fullName.toUpperCase();
        }

    @NotNull(message="Please specify Gender.",
            groups = {
                PartnerCreationValidator.class,
                ChurchMemberCreationValidator.class
    })
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull(message = "Specify whether the members is baptized or not.", groups={
            ChurchMemberCreationValidator.class
    })
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private BaptismStatus baptized;

    
    
    @Column(unique = true)
    private String profile;
    
    
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date dob;
    public void checkPhoneIsProvided() {
        if(this.getDob()==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Please specify the date of birth.");
        }
        LocalDate today = DateUtils.getNowDateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
                dobDate = this.getDob().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
        Period difference = Period.between(dobDate, today);
        if(difference.getYears()>=18) {
            if(this.getPhone()==null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Please provide the phone number!");
            }
        }
    }

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dom;

    @Column(columnDefinition = "BIT")
    private Boolean communion;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date communionDate;


    @Column(length = 50)
    private String email;

    // Partner
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Subscription Plan Invalid.", groups={
            PartnerCreationValidator.class
    })
    @Column(length = 10)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Member status Invalid.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(length = 10)
    private MemberStatus status;

    // Everyone
    @NotNull(message = "Language invalid.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AppLanguage language;

    @NotNull(message = "Please specify the category of the partner",
            groups={
                PartnerCreationValidator.class
    })
    @Size(min = 1, message = "At least one category should be selected",
            groups={
                PartnerCreationValidator.class
            })
    @Convert(converter = SetOfLongConverter.class)
    @Column(length = Integer.MAX_VALUE)
    private Set<Long> categoriesIds;
    public void addCategory(Long category){
        if(this.getCategoriesIds()==null) this.categoriesIds = new HashSet<>();
        if(!this.getCategoriesIds().contains(category)) this.categoriesIds.add(category);
    }

    @Column(length = Integer.MAX_VALUE)
    @Convert(converter = SetOfLongConverter.class)
    private Set<Long> contributionsIds;
    public void addContribution(Long contributionId){
        if(this.getCategoriesIds()==null) this.contributionsIds = new HashSet<>();
        if(!this.getContributionsIds().contains(contributionId)) this.contributionsIds.add(contributionId);
    }

    @PastOrPresent(message = "The date of creation must be a current date or a date in the past.")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = DateUtils.getNowDateTime();
    public void setCreationDate(@PastOrPresent(message = "The date of creation must be a current date or a date in the past.") Date creationDate) {
        if(creationDate==null) creationDate = DateUtils.getNowDateTime();
        this.creationDate = creationDate;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Transient
    private MemberOfFamilyTitle familyRole;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Transient
    private Date memberSince;

    @JsonIgnore
    private Long baptist;

    @JsonIgnore
    private Long guestBaptist;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dateOfBaptism;

    @Enumerated(EnumType.STRING)
    private ChurchFunction churchFunction;

    @Column(columnDefinition = "BIT")
    private boolean active = true;
    public void toggleActive(){
        this.setActive(!this.isActive());
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean newPinCode;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date deadline;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonManagedReference
    private Set<MemberDTO> familyMembers;


    @Transient
    @JsonIgnore
    private boolean imported;
    public void adjustDeadline(boolean isUpdate){
        if(this.getSubscription()!=null){
            if(!Objects.equals(this.getSubscription(), Subscription.LIFETIME)) {
                Calendar calendar = Calendar.getInstance();
                if (this.isImported()) calendar.setTime(DateUtils.getNowDateTime());
                else calendar.setTime(isUpdate ? this.getDeadline() : this.getCreationDate());
                switch (this.getSubscription()) {
                    case ANNUAL:
                        calendar.add(Calendar.YEAR, 1);
                        this.setDeadline(calendar.getTime());
                        break;

                    case SEMESTRAL:
                        calendar.add(Calendar.MONTH, 6);
                        this.setDeadline(calendar.getTime());
                        break;

                    case MONTHLY:
                        calendar.add(Calendar.MONTH, 1);
                        this.setDeadline(calendar.getTime());
                        break;

                    case QUARTER:
                        calendar.add(Calendar.MONTH, 3);
                        this.setDeadline(calendar.getTime());
                        break;

                    case WEEKLY:
                        calendar.add(Calendar.DAY_OF_YEAR, 7);
                        this.setDeadline(calendar.getTime());
                        break;
                }
            }
        }
    }
    public void adjust(){
        if(this.getCreationDate()==null) this.setCreationDate(DateUtils.getNowDateTime());
        this.adjustDeadline(false);
    }

    @JsonIgnore
    @Transient
    public void computeMemberCode(Institution institution,
                                  AppRepository appRepository){
        String newMemberCode = institution.computeMemberCode(appRepository);
        this.setCode(newMemberCode);
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean deletable = false;


    @JsonIgnore
    @Transient
    public static Page<InstitutionMember> getListOfMembers(@NonNull EntityManager entityManager,
                                                            @NonNull AppRepository appRepository,
                                                            @NonNull ObjectMapper objectMapper,
                                                            @NotNull MemberListingQuery listingQuery){
        Page<InstitutionMember> foundResults;
        try {
            String selectQuery = "SELECT DISTINCT m FROM InstitutionMember m",
                    countQuery = "SELECT COUNT(m) FROM InstitutionMember m",
                    conditionString = " WHERE m.institutionId=:institutionId";
            if (!StringUtils.isEmpty(listingQuery.getQuery())) {
                conditionString += " AND (LOWER(m.firstName) LIKE LOWER(:query) OR LOWER(m.lastName) LIKE LOWER(:query) OR LOWER(CONCAT(m.firstName, ' ', m.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(m.lastName, ' ', m.firstName)) LIKE LOWER(:query))";
            }

            if (listingQuery.getPhone() != null) conditionString += " AND CONCAT(CONVERT(VARCHAR, m.phoneCode), CONVERT(VARCHAR,m.phone))=CONVERT(VARCHAR, :phone)";
            if (listingQuery.getAlternatePhone() != null) conditionString += " AND CONCAT(CONVERT(VARCHAR, m.alternatePhoneCode), CONVERT(VARCHAR, m.alternatePhone))=CONVERT(VARCHAR,:alternatePhone)";
            if (listingQuery.getSubscription() != null) conditionString += " AND m.subscription=:subscription";
            if (listingQuery.getPincode() != null) conditionString += " AND m.pincode=:pincode";
            if (listingQuery.getCountries()!=null && !listingQuery.getCountries().isEmpty()) conditionString += " AND m.country IN (:countries)";
            if (listingQuery.getActive()!=null) conditionString += " AND m.active=:memberActive";
            if (!StringUtils.isEmpty(listingQuery.getDistrict())) conditionString += " AND m.district=:district";


            Map<String, String> partnersCodesMap = new HashMap<>();
            if (listingQuery.getPartnerCodes() != null && !listingQuery.getPartnerCodes().isEmpty()) {
                List<String> partnersCodeList = new ArrayList<>();
                listingQuery.getPartnerCodes()
                        .parallelStream()
                        .forEach(partnerCode->{
                            String categoryPlaceholder = "code_"+AppStringUtils.generateRandomString(4);
                            if(partnersCodesMap.containsKey(categoryPlaceholder)) categoryPlaceholder = "code_"+AppStringUtils.generateRandomString(4);
                            partnersCodesMap.put(categoryPlaceholder, partnerCode.toLowerCase()+'%');
                            partnersCodeList.add("LOWER(m.code) LIKE :"+categoryPlaceholder);
                        });

                if(!partnersCodeList.isEmpty()) {
                    conditionString += " AND (";
                    conditionString += String.join(" OR ", partnersCodeList);
                    conditionString += ")";
                }
            }


            Map<String, String> categoriesMap = new HashMap<>();
            if (listingQuery.getCategories() != null && !listingQuery.getCategories().isEmpty()) {
                conditionString += " AND (";
                List<String> categoriesConditions = new ArrayList<>();
                listingQuery.getCategories().parallelStream()
                        .forEach(singleCategory->{
                            String placeholder = "category_"+AppStringUtils.generateRandomString(4);
                            if(categoriesMap.containsKey(placeholder)) placeholder = "category_"+AppStringUtils.generateRandomString(4);
                            categoriesMap.put(placeholder, "%,"+singleCategory+",%");
                            categoriesConditions.add("CONCAT(',', m.categoriesIds,',') LIKE :"+placeholder);
                        });
                conditionString += String.join(" OR ", categoriesConditions);
                conditionString += ")";
            }


            if (listingQuery.getStates() != null && !listingQuery.getStates().isEmpty()) {
                conditionString += " AND m.state IN (:countryStates)";
            } else {
                if (!StringUtils.isEmpty(listingQuery.getState())) conditionString += " AND m.state=:countryStates";
            }

            switch (listingQuery.getCreationDateIn()) {
                case TODAY:
                case SINGLE_DATE:
                    conditionString += " AND CONVERT(DATE, m.creationDate)=CONVERT(DATE, :dateFrom)";
                    break;

                case LAST_SEVEN_DAYS:
                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                case YEAR:
                case CUSTOM_DATE_GAP:
                    conditionString += " AND CONVERT(DATE, m.creationDate)>=CONVERT(DATE, :dateFrom) AND CONVERT(DATE, m.creationDate)<=CONVERT(DATE, :dateTo)";
                    break;
            }


            TypedQuery<InstitutionMember> selectTypedQuery = entityManager
                    .createQuery(selectQuery + conditionString, InstitutionMember.class);

            TypedQuery<Long> countTypedQuery = entityManager
                    .createQuery(countQuery + conditionString, Long.class);

            selectTypedQuery.setParameter("institutionId", listingQuery.getInstitutionId());
            countTypedQuery.setParameter("institutionId", listingQuery.getInstitutionId());

            if (!StringUtils.isEmpty(listingQuery.getQuery())) {
                selectTypedQuery.setParameter("query", "%" + listingQuery.getQuery().trim() + "%");
                countTypedQuery.setParameter("query", "%" + listingQuery.getQuery().trim() + "%");
            }

            if(!partnersCodesMap.isEmpty()){
                partnersCodesMap.keySet()
                        .parallelStream()
                                .forEach(key->{
                                    selectTypedQuery.setParameter(key, partnersCodesMap.get(key));
                                    countTypedQuery.setParameter(key, partnersCodesMap.get(key));
                                });
            }

            if(!categoriesMap.isEmpty()){
                categoriesMap.keySet()
                        .parallelStream()
                        .forEach(key->{
                            selectTypedQuery.setParameter(key, categoriesMap.get(key));
                            countTypedQuery.setParameter(key, categoriesMap.get(key));
                        });
            }

            if (listingQuery.getSubscription() != null){
                selectTypedQuery.setParameter("subscription", listingQuery.getSubscription());
                countTypedQuery.setParameter("subscription", listingQuery.getSubscription());
            }

            if (listingQuery.getPincode() != null) {
                selectTypedQuery.setParameter("pincode", listingQuery.getPincode());
                countTypedQuery.setParameter("pincode", listingQuery.getPincode());
            }

            if (listingQuery.getCountries()!=null && !listingQuery.getCountries().isEmpty()) {
                selectTypedQuery.setParameter("countries", listingQuery.getCountries());
                countTypedQuery.setParameter("countries", listingQuery.getCountries());
            }
            if (listingQuery.getStates() != null && !listingQuery.getStates().isEmpty()) {
                selectTypedQuery.setParameter("countryStates", listingQuery.getStates());
                countTypedQuery.setParameter("countryStates", listingQuery.getStates());
            } else {
                if (!StringUtils.isEmpty(listingQuery.getState())) {
                    selectTypedQuery.setParameter("countryStates", listingQuery.getState());
                    countTypedQuery.setParameter("countryStates", listingQuery.getState());
                }
            }

            if (!StringUtils.isEmpty(listingQuery.getDistrict())) {
                selectTypedQuery.setParameter("district", listingQuery.getDistrict());
                countTypedQuery.setParameter("district", listingQuery.getDistrict());
            }

            if(listingQuery.getActive()!=null){
                selectTypedQuery.setParameter("memberActive", listingQuery.getActive());
                countTypedQuery.setParameter("memberActive", listingQuery.getActive());
            }

            if (listingQuery.getPhone() != null) {
                selectTypedQuery.setParameter("phone", listingQuery.getPhone());
                countTypedQuery.setParameter("phone", listingQuery.getPhone());
            }

            if (listingQuery.getAlternatePhone() != null) {
                selectTypedQuery.setParameter("alternatePhone", listingQuery.getAlternatePhone());
                countTypedQuery.setParameter("alternatePhone", listingQuery.getAlternatePhone());
            }

            Calendar calendar = Calendar.getInstance();
            Date dateOne, dateTwo,
                    today = DateUtils.convertToTimezone(DateUtils.getNowDateTime(),
                            listingQuery.getClientTimezone());
            switch (listingQuery.getCreationDateIn()) {
                case TODAY:
                case SINGLE_DATE:
                    if (listingQuery.getCreationDateIn().equals(DurationGap.SINGLE_DATE) && listingQuery.getFrom() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date(s) not specified.");
                    }
                    dateOne = (listingQuery.getCreationDateIn().equals(DurationGap.TODAY))
                            ? today
                            : DateUtils.convertToTimezone(listingQuery.getFrom(), listingQuery.getClientTimezone());
                    selectTypedQuery.setParameter("dateFrom", dateOne);
                    countTypedQuery.setParameter("dateFrom", dateOne);
                    break;

                case LAST_SEVEN_DAYS:
                    calendar.add(Calendar.DAY_OF_WEEK, -7);
                    dateTwo = DateUtils.convertToTimezone(calendar.getTime(), listingQuery.getClientTimezone());
                    selectTypedQuery.setParameter("dateFrom", dateTwo);
                    countTypedQuery.setParameter("dateFrom", dateTwo);
                    selectTypedQuery.setParameter("dateTo", today);
                    countTypedQuery.setParameter("dateTo", today);
                    break;

                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                    int months = -1;
                    if (listingQuery.getCreationDateIn().equals(DurationGap.LAST_THREE_MONTH)) months = -3;
                    if (listingQuery.getCreationDateIn().equals(DurationGap.LAST_SIX_MONTH)) months = -6;
                    calendar.add(Calendar.MONTH, months);
                    dateTwo = DateUtils.convertToTimezone(calendar.getTime(), listingQuery.getClientTimezone());
                    selectTypedQuery.setParameter("dateFrom", dateTwo);
                    countTypedQuery.setParameter("dateFrom", dateTwo);
                    selectTypedQuery.setParameter("dateTo", today);
                    countTypedQuery.setParameter("dateTo", today);
                    break;

                case YEAR:
                    calendar.add(Calendar.YEAR, -1);
                    dateTwo = DateUtils.convertToTimezone(calendar.getTime(), listingQuery.getClientTimezone());
                    selectTypedQuery.setParameter("dateFrom", dateTwo);
                    countTypedQuery.setParameter("dateFrom", dateTwo);
                    selectTypedQuery.setParameter("dateTo", today);
                    countTypedQuery.setParameter("dateTo", today);
                    break;

                case CUSTOM_DATE_GAP:
                    if (listingQuery.getFrom() == null || listingQuery.getTo() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date(s) not specified.");
                    }
                    if (listingQuery.getFrom().after(listingQuery.getTo())) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Ending date must be a date after the Starting date.");
                    }
                    selectTypedQuery.setParameter("dateFrom", listingQuery.getFrom());
                    countTypedQuery.setParameter("dateFrom", listingQuery.getFrom());
                    selectTypedQuery.setParameter("dateTo", listingQuery.getTo());
                    countTypedQuery.setParameter("dateTo", listingQuery.getTo());
                    break;
            }

            // Set pagination parameters
            int page = listingQuery.getPage() - 1;
            if (page < 0) page = 0;
            selectTypedQuery.setFirstResult(page * listingQuery.getSize());
            selectTypedQuery.setMaxResults(listingQuery.getSize());

            List<InstitutionMember> results = InstitutionMember
                    .populateSearchedMember(selectTypedQuery.getResultList(),
                            appRepository, listingQuery.getInstitution());

            Long totalCount = countTypedQuery.getSingleResult();

            if (totalCount == null) totalCount = 0L;
            foundResults = new PageImpl<>(results,
                    PageRequest.of(page, listingQuery.getSize()),
                    totalCount);
        }  catch(Exception exception){
            exception.printStackTrace();
            LOGGER.info("Members listing : "+exception.getMessage());
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to execute your request. Please try again later.");
        } finally {
            entityManager.close();
        }
        return foundResults;
    }


    @Transient
    public static List<InstitutionMember> populateSearchedMember(@NonNull List<InstitutionMember> members,
                                                                 @NonNull AppRepository appRepository,
                                                                 @NonNull Institution institution){
        Set<Long> foundPeopleIds = members
                .parallelStream()
                .map(InstitutionMember::getId)
                .collect(Collectors.toSet());


        // Get all the family members of the found persons
        List<InstitutionFamilyMember> initialMembersList = appRepository
                .getInstitutionFamilyMemberRepository()
                .getAllFamiliesOfMembers(foundPeopleIds);

        Set<Long> familiesIds = initialMembersList.parallelStream()
                .map(InstitutionFamilyMember::getFamilyId)
                .collect(Collectors.toSet());

        List<InstitutionFamilyMember> familyMemberSet = appRepository
                .getInstitutionFamilyMemberRepository()
                .findAllByFamilyIdIn(familiesIds);

        // Get the Ids of the found persons
        Set<Long> allFamiliesMembersIds = familyMemberSet
                .parallelStream()
                .map(InstitutionFamilyMember::getMemberId)
                .collect(Collectors.toSet());

        List<InstitutionMember> familiesMembersList = appRepository
                .getInstitutionMemberRepository()
                .findAllByIdIn(allFamiliesMembersIds);
        Map<Long, InstitutionMember> familiesMembers = familiesMembersList
                .parallelStream()
                .collect(Collectors.toMap(InstitutionMember::getId, item->item));



        Map<Long, Set<InstitutionFamilyMember>> mapOfMembersPerMemberId = new HashMap<>();
        familyMemberSet.parallelStream()
                        .forEach(singleFamilyMember->{
                            if(!mapOfMembersPerMemberId.containsKey(singleFamilyMember.getMemberId())){
                                mapOfMembersPerMemberId.put(singleFamilyMember.getMemberId(), new HashSet<>());
                            }
                            Set<InstitutionFamilyMember> sameFamiliesGuys = familyMemberSet
                                    .parallelStream()
                                    .filter(sameFamilyMember -> {
                                        // Filteringa based in this inputs
                                        return sameFamilyMember.getFamilyId().equals(singleFamilyMember.getFamilyId())
                                                && !Objects.equals(sameFamilyMember.getMemberId(), singleFamilyMember.getMemberId());
                                    })
                                    .collect(Collectors.toSet());
                            if(mapOfMembersPerMemberId.containsKey(singleFamilyMember.getMemberId())) {
                                mapOfMembersPerMemberId.get(singleFamilyMember.getMemberId()).addAll(sameFamiliesGuys);
                            }
                        });


        members.parallelStream()
                .forEach(member->{
                    Set<InstitutionFamilyMember> singleMemberRelated = mapOfMembersPerMemberId.get(member.getId());
                    if(singleMemberRelated!=null && !singleMemberRelated.isEmpty()) {
                        List<InstitutionMember> familyMembers = singleMemberRelated
                                .parallelStream()
                                .map(familyMember-> {
                                    InstitutionMember theMember = familiesMembers.get(familyMember.getMemberId());
                                    if(theMember==null) return null;
                                    theMember.setFamilyRole(familyMember.getTitle());
                                    return theMember;
                                })
                                .filter(m->!Objects.equals(m, null))
                                .collect(Collectors.toList());

                        Set<MemberDTO> membersDTO = familyMembers
                                .stream()
                                .map(singleMember->{
                                    MemberDTO memberDTO = new MemberDTO();
                                    memberDTO.setLastName(singleMember.getLastName());
                                    memberDTO.setFirstName(singleMember.getFirstName());
                                    memberDTO.setPhoneCode(singleMember.getPhoneCode());
                                    memberDTO.setPhone(singleMember.getPhone());
                                    memberDTO.setCode(singleMember.getCode());
                                    memberDTO.setFamilyRole(singleMember.getFamilyRole());
                                    return memberDTO;
                        }).collect(Collectors.toSet());
                        member.setFamilyMembers(membersDTO);
                    }



                    Map<Long, String> subscriptionsMap = institution.getInstitutionType().equals(InstitutionType.GENERAL)
                            ? appRepository.getCategoryRepository().findAllByInstitutionId(institution.getId())
                                .parallelStream()
                                .collect(Collectors.toMap(InstitutionCategory::getId, InstitutionCategory::getName))
                            : appRepository.getChurchContributionRepository().findAllByInstitutionId(institution.getId())
                                .parallelStream()
                                .collect(Collectors.toMap(ChurchContribution::getId, ChurchContribution::getName));

                    PageRequest pageRequest = (institution.getInstitutionType().equals(InstitutionType.CHURCH))
                            ? PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, ChurchContribution.Fields.id))
                            : PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, InstitutionCategory.Fields.id));
                    // Get all the subscriptions
                    Set<Long> subscribedTos = (institution.getInstitutionType().equals(InstitutionType.CHURCH))
                            ? member.getContributionsIds()
                            : member.getCategoriesIds();

                    if(subscribedTos!=null){
                    subscribedTos.parallelStream()
                            .forEach(singleSubscription->{
                                List<PeopleSubscription> theSubscriptions;
                                if(!member.getMemberSubscriptions().isEmpty()){
                                    theSubscriptions = member.getMemberSubscriptions()
                                            .parallelStream()
                                            .filter(theSingleSubscription->{
                                                if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
                                                    return theSingleSubscription.getContributionId().equals(singleSubscription);
                                                } else {
                                                    return theSingleSubscription.getCategoryId().equals(singleSubscription);
                                                }
                                            }).collect(Collectors.toList());
                                } else {
                                    theSubscriptions = institution.getInstitutionType().equals(InstitutionType.CHURCH)
                                            ? appRepository.getPeopleSubscriptionRepository()
                                            .getLastContribution(member.getId(),
                                                    institution.getId(),
                                                    singleSubscription,
                                                    pageRequest)
                                            : appRepository.getPeopleSubscriptionRepository()
                                            .getLastDonation(member.getId(),
                                                    institution.getId(),
                                                    singleSubscription,
                                                    pageRequest);
                                }

                                if(theSubscriptions.isEmpty()){
                                    LOGGER.info("Kiri empty wangu");
                                    PeopleSubscription newSubscription = member.correctSubscriptions(appRepository,
                                            institution,
                                            singleSubscription);
                                    theSubscriptions.add(newSubscription);
                                }


                                if(subscriptionsMap.containsKey(singleSubscription)){
                                    String name = subscriptionsMap.get(singleSubscription);
                                    SubscriptionData subscriptiondata = SubscriptionData.builder()
                                            .deadline(theSubscriptions.get(0).getDeadline())
                                            .subscription(theSubscriptions.get(0).getSubscription())
                                            .build();
                                    member.getSubscriptionsMap().put(name, subscriptiondata);
                                }
                            });
                    }
                });

        return members;
    }

    private static String addCountryBasedQuery(@NonNull AddressRequestModel requestModel){
        List<String> country = new ArrayList<>();
        if (requestModel.getCountry() !=null && !requestModel.getCountry().isEmpty()) country.add("m.country IN :country");
        if (requestModel.getPhone() != null) country.add("CONCAT(CONVERT(VARCHAR,m.phoneCode), CONVERT(VARCHAR,m.phone))=CONVERT(VARCHAR, :phone)");
        if (requestModel.getAlternatePhone() != null) country.add("CONCAT(CONVERT(VARCHAR,m.alternatePhone), CONVERT(VARCHAR,m.alternatePhone))=CONVERT(VARCHAR, :alternatePhone)");
        if (!StringUtils.isEmpty(requestModel.getDistrict())) country.add("m.district=:district");
        return String.join(" AND ", country);

    }
    private static String addReceiptCategoryQuery(@NonNull AddressRequestModel requestModel){
        List<String> queries = new ArrayList<>();
        if(requestModel.getWithReceipt()!=null && requestModel.getWithReceipt()){
            if (requestModel.getMinAmount() != null || requestModel.getMaxAmount() != null) {
                if (requestModel.getMinAmount() != null) queries.add("isd.amount>=:minAmount");
                if (requestModel.getMaxAmount() != null) queries.add("isd.amount<=:maxAmount");
            }

            if (requestModel.getPaymentMode() != null && !requestModel.getPaymentMode().isEmpty()) {
                queries.add("inr.paymentMode IN (:paymentModes)");
            }

            if (requestModel.getCreditAccountId() != null) {
                queries.add("inr.creditAccountId=:creditAccountId");
            }
        }
        if (requestModel.getCategories() != null && !requestModel.getCategories().isEmpty()) {
            queries.add((requestModel.getInstitution().getInstitutionType()
                    .equals(InstitutionType.GENERAL))
                    ? "isd.categoryId IN (:categoriesList)"
                    : "isd.churchContributionId IN (:categoriesList)");
        }
        return String.join(" AND ", queries);
    }

    private static String addCategoryQuery(@NonNull AddressRequestModel requestModel){
        String output = "";
        if (requestModel.getCategories() != null && !requestModel.getCategories().isEmpty()) {
            List<String> categoriesStrings = new ArrayList<>();
            for(Long categoryId : requestModel.getCategories()){
                String columnName = (requestModel.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL))
                        ? "m.categoriesIds" : "m.contributionsIds";
                categoriesStrings.add("CONCAT(',', "+columnName+", ',') LIKE '%,"+categoryId+",%'");
            }
            if(!categoriesStrings.isEmpty()) {
                String categoryQueryString = String.join(" OR ", categoriesStrings);
                output = "(" + categoryQueryString + ")";
            }
        }
        return output;
    }



    @JsonIgnore
    @Transient
    public static Page<InstitutionMember> getMembersAddresses(@NotNull EntityManager entityManager,
                                                              @NotNull AppRepository appRepository,
                                                              AddressRequestModel requestModel){
        Page<InstitutionMember> foundResults;
        try {
            String commonString;
            if(requestModel.getWithReceipt()!=null){
                commonString = requestModel.getWithReceipt()
                        ? " JOIN InstitutionDonation isd ON m.id=isd.memberId JOIN InstitutionReceipt inr ON isd.receiptNo=inr.receiptNo"
                        : " LEFT JOIN InstitutionDonation isd ON m.id=isd.memberId";
            } else {
                commonString = " LEFT JOIN InstitutionDonation isd ON (m.id=isd.memberId AND m.institutionId=isd.institutionId)";
            }
            String sqlString = "SELECT DISTINCT m FROM InstitutionMember m",
                    countQueryString = "SELECT COUNT(DISTINCT(m)) FROM InstitutionMember m",
                    conditionString = " WHERE (m.institutionId=:institutionId";

            if (!AppStringUtils.isEmpty(requestModel.getQuery())) {
                conditionString += " AND (LOWER(m.firstName) LIKE LOWER(:query) OR LOWER(m.lastName) LIKE LOWER(:query) OR LOWER(CONCAT(m.firstName, ' ', m.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(m.lastName, ' ', m.firstName)) LIKE LOWER(:query))";
            }

            String countryBasedQuery = InstitutionMember.addCountryBasedQuery(requestModel);
            if(!StringUtils.isEmpty(countryBasedQuery)) conditionString += " AND "+countryBasedQuery;

            if (requestModel.getLanguages() != null && !requestModel.getLanguages().isEmpty())
                conditionString += " AND m.language IN(:languagesList)";
            if(requestModel.getWithReceipt()!=null) {
                if(requestModel.getWithReceipt()) {
                    String categoryReceiptQuery = InstitutionMember.addReceiptCategoryQuery(requestModel);
                    if(!StringUtils.isEmpty(categoryReceiptQuery)) conditionString += " AND "+categoryReceiptQuery;
                } else {
                    if(requestModel.getCategories()!=null && !requestModel.getCategories().isEmpty()) {
                        List<String> noPaymentQuery = new ArrayList<>();
                        String categoryQuery = InstitutionMember.addCategoryQuery(requestModel);
                        if (!StringUtils.isEmpty(categoryQuery)) noPaymentQuery.add(categoryQuery);

                        // noPaymentQuery.add("inr.id IS NULL");
                        if(!noPaymentQuery.isEmpty()) conditionString += " AND (" + String.join(" AND ", noPaymentQuery) + ")";
                    } else {
                        conditionString += " AND isd.id IS NULL";
                    }
                }
            } else {
                List<String> noPaymentQuery = new ArrayList<>();
                if(requestModel.getCategories()!=null && !requestModel.getCategories().isEmpty()) {
                    String categoryQuery = InstitutionMember.addCategoryQuery(requestModel);
                    if (!StringUtils.isEmpty(categoryQuery)) noPaymentQuery.add(categoryQuery);

                    // noPaymentQuery.add("inr.id IS NULL");
                    if(!noPaymentQuery.isEmpty()) conditionString += " AND ("+String.join(" AND ", noPaymentQuery)+")";

                    String categoryReceiptQuery = InstitutionMember.addReceiptCategoryQuery(requestModel);
                    if(!StringUtils.isEmpty(categoryReceiptQuery)) conditionString += " OR ("+categoryReceiptQuery+")";
                    if(!StringUtils.isEmpty(countryBasedQuery)) conditionString += " AND "+countryBasedQuery;
                }
            }

            switch (requestModel.getDuration()) {
                case TODAY:
                case SINGLE_DATE:
                    if(requestModel.getWithReceipt()!=null) {
                        if(requestModel.getWithReceipt()){
                            conditionString += " AND CONVERT(DATE, inr.entryDate)=CONVERT(DATE, :fromDate)";
                        } else {
                            conditionString += " AND CONVERT(DATE, m.creationDate)=CONVERT(DATE, :fromDate)";
                        }
                    } else {
                        conditionString += " AND (";
                        conditionString += "CONVERT(DATE, isd.entryDate)=CONVERT(DATE, :fromDate)";
                        conditionString += " OR CONVERT(DATE, m.creationDate)=CONVERT(DATE, :fromDate)";
                        conditionString += ")";
                    }
                    break;

                case LAST_SEVEN_DAYS:
                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                case YEAR:
                case CUSTOM_DATE_GAP:
                    if(requestModel.getWithReceipt()!=null) {
                        if (requestModel.getWithReceipt()) {
                            conditionString += " AND (CONVERT(DATE, inr.entryDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        } else {
                            conditionString += " AND (CONVERT(DATE, m.creationDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        }
                    } else {
                        conditionString += " AND (";
                        conditionString += "(CONVERT(DATE, isd.entryDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        conditionString += " OR (CONVERT(DATE, m.creationDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        conditionString += ")";
                    }
                    break;
            }

            if (requestModel.getPincode() != null) conditionString += " AND m.pincode=:pincode";

            if (requestModel.getPincode() != null) conditionString += " AND m.pincode=:pincode";
            if (requestModel.getStates() != null && !requestModel.getStates().isEmpty()) {
                conditionString += " AND m.state IN (:addressStates)";
            } else {
                if (!StringUtils.isEmpty(requestModel.getState())) {
                    conditionString += " AND m.state=:addressStates";
                }
            }

            if (requestModel.getMemberCodes() != null && !requestModel.getMemberCodes().isEmpty()) {
                conditionString += " AND LOWER(m.code) IN :memberCodes";
            }

            if (requestModel.getLanguages() != null && !requestModel.getLanguages().isEmpty()) {
                conditionString += " AND m.language IN :languagesList";
            }


            if (requestModel.getSubscription() != null && !requestModel.getSubscription().isEmpty()) {
                commonString += " INNER JOIN PeopleSubscription psubs ON psubs.memberId=m.id";
                conditionString += " AND psubs.subscription IN :subcriptionsList";
            }
            conditionString += ")";
            if(requestModel.getActive()!=null) conditionString += " AND m.active=:memberActive";
            String groupBy = " "; //GROUP BY m.id, m.firstName, m.lastName";

            LOGGER.info(sqlString + commonString + conditionString + groupBy);
            TypedQuery<InstitutionMember> query = entityManager
                    .createQuery(sqlString + commonString + conditionString + groupBy,
                            InstitutionMember.class);

            TypedQuery<Long> countQuery = entityManager
                    .createQuery(countQueryString + commonString + conditionString,
                            Long.class);

            query.setParameter("institutionId", requestModel.getInstitutionId());
            countQuery.setParameter("institutionId", requestModel.getInstitutionId());


            if(requestModel.getActive()!=null) {
                boolean isMemberActive = requestModel.getActive();
                query.setParameter("memberActive", isMemberActive);
                countQuery.setParameter("memberActive", isMemberActive);
            }
            if (!StringUtils.isEmpty(requestModel.getQuery())) {
                query.setParameter("query", "%"+requestModel.getQuery().trim()+"%");
                countQuery.setParameter("query", "%"+requestModel.getQuery().trim()+"%");
            }
            if (requestModel.getPincode() != null) {
                query.setParameter("pincode", requestModel.getPincode());
                countQuery.setParameter("pincode", requestModel.getPincode());
            }
            if (requestModel.getPhone() != null) {
                query.setParameter("phone", requestModel.getPhone());
                countQuery.setParameter("phone", requestModel.getPhone());
            }
            if (requestModel.getAlternatePhone() != null) {
                query.setParameter("alternatePhone", requestModel.getAlternatePhone());
                countQuery.setParameter("alternatePhone", requestModel.getAlternatePhone());
            }
            if (requestModel.getAlternatePhone() != null) {
                query.setParameter("alternatePhone", requestModel.getAlternatePhone());
                countQuery.setParameter("alternatePhone", requestModel.getAlternatePhone());
            }
            if (requestModel.getCountry()!=null && !requestModel.getCountry().isEmpty()){
                query.setParameter("country", requestModel.getCountry());
                countQuery.setParameter("country", requestModel.getCountry());
            }
            if (requestModel.getStates() != null && !requestModel.getStates().isEmpty()) {
                query.setParameter("addressStates", requestModel.getStates());
                countQuery.setParameter("addressStates", requestModel.getStates());
            } else {
                if (!StringUtils.isEmpty(requestModel.getState())) {
                    query.setParameter("addressStates", requestModel.getState());
                    countQuery.setParameter("addressStates", requestModel.getState());
                }
            }
            if (!StringUtils.isEmpty(requestModel.getDistrict())){
                query.setParameter("district", requestModel.getDistrict().trim());
                countQuery.setParameter("district", requestModel.getDistrict().trim());
            }

            if(requestModel.getWithReceipt()!=null && requestModel.getWithReceipt()) {
                if (requestModel.getMinAmount() != null) {
                    query.setParameter("minAmount", requestModel.getMinAmount());
                    countQuery.setParameter("minAmount", requestModel.getMinAmount());
                }
                if (requestModel.getMaxAmount() != null) {
                    query.setParameter("maxAmount", requestModel.getMaxAmount());
                    countQuery.setParameter("maxAmount", requestModel.getMaxAmount());
                }

                if (requestModel.getPaymentMode() != null && !requestModel.getPaymentMode().isEmpty()) {
                    query.setParameter("paymentModes", requestModel.getPaymentMode());
                    countQuery.setParameter("paymentModes", requestModel.getPaymentMode());
                }
                if (requestModel.getCreditAccountId() != null) {
                    query.setParameter("creditAccountId", requestModel.getCreditAccountId());
                    countQuery.setParameter("creditAccountId", requestModel.getCreditAccountId());
                }
            }

            if (requestModel.getMemberCodes() != null && !requestModel.getMemberCodes().isEmpty()) {
                List<String> lowercaseMemberCodes = requestModel.getMemberCodes()
                                .parallelStream()
                                        .map(String::toLowerCase)
                                                .collect(Collectors.toList());
                query.setParameter("memberCodes", lowercaseMemberCodes);
                countQuery.setParameter("memberCodes", lowercaseMemberCodes);
            }


            if(requestModel.getWithReceipt()!=null) {
                if(requestModel.getWithReceipt()) {
                    if (requestModel.getCategories() != null && !requestModel.getCategories().isEmpty()) {
                        query.setParameter("categoriesList", requestModel.getCategories());

                        countQuery.setParameter("categoriesList", requestModel.getCategories());
                    }
                }
            } else {
                if (requestModel.getCategories() != null && !requestModel.getCategories().isEmpty()) {
                    query.setParameter("categoriesList", requestModel.getCategories());
                    countQuery.setParameter("categoriesList", requestModel.getCategories());
                }
            }


            if (requestModel.getLanguages() != null && !requestModel.getLanguages().isEmpty()) {
                query.setParameter("languagesList", requestModel.getLanguages());
                countQuery.setParameter("languagesList", requestModel.getLanguages());
            }

            if (requestModel.getSubscription() != null && !requestModel.getSubscription().isEmpty()) {
                query.setParameter("subcriptionsList", requestModel.getSubscription());
                countQuery.setParameter("subcriptionsList", requestModel.getSubscription());
            }

            Calendar calendar = Calendar.getInstance();
            Date dateTwo,
                    today = DateUtils.getNowDateTime();
            today = DateUtils.atTheBeginningOfTheDay(today);
            switch (requestModel.getDuration()) {
                case TODAY:
                case SINGLE_DATE:
                    if (requestModel.getDuration().equals(DurationGap.SINGLE_DATE) && requestModel.getFrom() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date(s) not specified.");
                    }
                    Date chosenDate = (requestModel.getDuration().equals(DurationGap.TODAY))
                            ? today
                            : requestModel.getFrom();
                    query.setParameter("fromDate", chosenDate);
                    countQuery.setParameter("fromDate", chosenDate);
                    break;

                case LAST_SEVEN_DAYS:
                    calendar.add(Calendar.DAY_OF_WEEK, -7);
                    dateTwo = calendar.getTime();
                    query.setParameter("fromDate", dateTwo);
                    countQuery.setParameter("fromDate", dateTwo);
                    query.setParameter("toDate", today);
                    countQuery.setParameter("toDate", today);
                    break;

                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                    int months = -1;
                    if (requestModel.getDuration().equals(DurationGap.LAST_THREE_MONTH)) months = -3;
                    if (requestModel.getDuration().equals(DurationGap.LAST_SIX_MONTH)) months = -6;
                    calendar.add(Calendar.MONTH, months);
                    dateTwo = calendar.getTime();
                    query.setParameter("fromDate", dateTwo);
                    countQuery.setParameter("fromDate", dateTwo);
                    query.setParameter("toDate", today);
                    countQuery.setParameter("toDate", today);
                    break;

                case YEAR:
                    dateTwo = DateUtils.getFirstDateOfYear(null);
                    query.setParameter("fromDate", dateTwo);
                    countQuery.setParameter("fromDate", dateTwo);
                    query.setParameter("toDate", today);
                    countQuery.setParameter("toDate", today);
                    break;

                case CUSTOM_DATE_GAP:
                    if (requestModel.getFrom() == null || requestModel.getTo() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date(s) not specified.");
                    }
                    if (requestModel.getFrom().after(requestModel.getTo())) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Ending date must be a date after the Starting date.");
                    }
                    Date fromDate = requestModel.getFrom(),
                            toDate = requestModel.getTo();
                    query.setParameter("fromDate", fromDate);
                    countQuery.setParameter("fromDate", fromDate);
                    query.setParameter("toDate", toDate);
                    countQuery.setParameter("toDate", toDate);
                    break;
            }


            if (requestModel.getLanguages() != null && !requestModel.getLanguages().isEmpty()) {
                query.setParameter("languagesList", requestModel.getLanguages());
                countQuery.setParameter("languagesList", requestModel.getLanguages());
            }
            // Set pagination parameters
            int page = requestModel.getPage() - 1;
            if (page < 0) page = 0;
            query.setFirstResult(page * requestModel.getSize());
            query.setMaxResults(requestModel.getSize());

            LinkedList<InstitutionMember> results = new LinkedList<>(query.getResultList());
            Map<Long, PersonTitle> titlesMap = GeneralValues.PERSON_TITLES;

            results.parallelStream().forEach(member -> {
                PersonTitle memberTitle = titlesMap.get(member.getTitleId());
                if(memberTitle!=null) member.setTitle(memberTitle.getShortName());
            });
            Long totalCount = countQuery.getSingleResult();
            totalCount = totalCount==null ? 0 : totalCount;
            foundResults = new PageImpl<>(results,
                    PageRequest.of(page, requestModel.getSize()),
                    totalCount);
        } catch(Exception exception){
            exception.printStackTrace();
            LOGGER.info("Address Printing : "+exception.getMessage());
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to execute your request. Please try again later.");
        } finally {
            entityManager.close();
        }
        return foundResults;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Map<String, SubscriptionData> subscriptionsMap = new HashMap<>();

    @Transient
    @JsonIgnore
    private List<PeopleSubscription> memberSubscriptions = new ArrayList<>();



    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Long> groupsIds = new HashSet<>();


    public void delete(AppRepository appRepository){
        // Delete in groups
        appRepository.getGroupMemberRepository().deleteAllByMemberId(this.getId());
        // Delete all donations
        appRepository.getInstitutionDonationRepository().deleteAllByMemberId(this.getId());

        appRepository.getInstitutionMemberRepository().deleteById(this.getId());
    }


    public InstitutionMember save(@NonNull AppRepository appRepository,
                                  @NonNull Institution institution){
        int tries = 0;
        InstitutionMember newMember = null;
        for(;tries<10; tries++) {
            if(StringUtils.isEmpty(this.getCode())) this.computeMemberCode(institution, appRepository);
            try {
                newMember = appRepository
                        .getInstitutionMemberRepository()
                        .save(this);
            } catch (Exception exception) {
                institution.setMembersCount(institution.getMembersCount() + 1);
                this.computeMemberCode(institution, appRepository);
            }

            tries++;
            if(newMember!=null) break;
        }
        if(newMember==null){
            CompletableFuture.runAsync(()->institution.autoCorrectData(appRepository));
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Sorry, we apologize, Please try after a moment.");
        }
        appRepository.getInstitutionRepository().save(institution);
        return newMember;
    }



    @Transient
    @JsonIgnore
    public static void addRegistrationStats(@Nullable Institution institution,
                                            @NonNull EntityManager entityManager,
                                            @NonNull DashboardStats stats){
        try {
            String countSql = "SELECT COUNT(m) FROM InstitutionMember m" +
                    " INNER JOIN Institution ins ON m.institutionId=ins.id";

            String churchCountSql = countSql + " WHERE ins.institutionType=:institutionType";
            String organizationCountSql = countSql + " WHERE ins.institutionType=:institutionType";
            String whereSelection = " AND (CONVERT(DATE, m.creationDate)>=CONVERT(DATE, :dateFrom) AND CONVERT(DATE, m.creationDate)<=CONVERT(DATE, :dateEnd))";
            if (institution != null) whereSelection += " AND ins.id=:institutionId";
            TypedQuery<Long> churchRegistrationsQuery = entityManager.createQuery(churchCountSql + whereSelection, Long.class),
                    orgRegistrationsQuery = entityManager.createQuery(organizationCountSql + whereSelection, Long.class);

            // Churchs
            churchRegistrationsQuery.setParameter("institutionType", InstitutionType.CHURCH);
            if (institution != null) churchRegistrationsQuery.setParameter("institutionId", institution.getId());
            // Organizations
            orgRegistrationsQuery.setParameter("institutionType", InstitutionType.GENERAL);
            if (institution != null) orgRegistrationsQuery.setParameter("institutionId", institution.getId());

            InstitutionMember.addRegistrationStats(stats,
                    institution,
                    churchRegistrationsQuery,
                    orgRegistrationsQuery);
        } finally {
            entityManager.close();
        }
    }


    private static void addRegistrationStats(@NonNull DashboardStats stats,
            @Nullable Institution institution,
            @NonNull TypedQuery<Long> churchRegistrationsQuery,
            @NonNull TypedQuery<Long> orgRegistrationsQuery){
Date today = DateUtils.getNowDateTime();

List<DashboardDuration> dailyValued = Arrays.asList(
DashboardDuration.WEEK,
DashboardDuration.FIFTEEN_DAYS,
DashboardDuration.MONTH);
List<DashboardDuration> datedDurations = Arrays.stream(DashboardDuration.values())
.filter(dailyValued::contains)
.collect(Collectors.toList());

datedDurations.stream()
.forEach(duration->{
// Changed to a List of Maps to allow proper sorting by date
List<Map<String, Object>> churchDurationStatList = new ArrayList<>();
List<Map<String, Object>> orgDurationStatList = new ArrayList<>();

DateGap dateGap = DashboardUtils.getDateGap(duration, today);
List<Date> days = DateUtils.getDatesBetween(dateGap.getFrom(), dateGap.getEnd());

// 'days' is already sorted chronologically by DateUtils.getDatesBetween
// So, adding them in order to the list ensures the list is sorted.

days.forEach(day->{
DateGap monthDayGap = DateGap.getSingleDayDateDap(day);
// Store the original Date object or a sortable string like "yyyy-MM-dd" for sorting
// and the display string "d MMM" for frontend display.
String displayDate = DateUtils.getMonthDayName(monthDayGap.getFrom());

if(institution == null || institution.getInstitutionType().equals(InstitutionType.CHURCH)){
churchRegistrationsQuery.setParameter("dateFrom", monthDayGap.getFrom());
churchRegistrationsQuery.setParameter("dateEnd", monthDayGap.getEnd());
Long churchRegistrations = churchRegistrationsQuery.getSingleResult();
churchRegistrations = (churchRegistrations==null) ? 0 : churchRegistrations;

Map<String, Object> entry = new LinkedHashMap<>(); // LinkedHashMap preserves insertion order within the map itself
entry.put("date", monthDayGap.getFrom()); // Use Date object for accurate sorting
entry.put("displayDate", displayDate);     // For frontend display (e.g., "6 Jul")
entry.put("value", churchRegistrations);
churchDurationStatList.add(entry);
}

if(institution == null || institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
orgRegistrationsQuery.setParameter("dateFrom", monthDayGap.getFrom());
orgRegistrationsQuery.setParameter("dateEnd", monthDayGap.getEnd());
Long organizationRegistrations = orgRegistrationsQuery.getSingleResult();
organizationRegistrations = (organizationRegistrations==null) ? 0 : organizationRegistrations;

Map<String, Object> entry = new LinkedHashMap<>();
entry.put("date", monthDayGap.getFrom());
entry.put("displayDate", displayDate);
entry.put("value", organizationRegistrations);
orgDurationStatList.add(entry);
}
});

// After adding all entries, sort the list by the 'date' field
churchDurationStatList.sort(Comparator.comparing(m -> (Date)m.get("date")));
orgDurationStatList.sort(Comparator.comparing(m -> (Date)m.get("date")));

if(institution == null || institution.getInstitutionType().equals(InstitutionType.CHURCH)) {
// Type should be Map<DashboardDuration, List<Map<String, Object>>>
if(stats.getChurchRegistrations()==null) {
Map<DashboardDuration, List<Map<String, Object>>> churchRegistrations = new HashMap<>();
stats.setChurchRegistrations(churchRegistrations);
}
stats.getChurchRegistrations().put(duration, churchDurationStatList);
}
if(institution == null || institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
if(stats.getOrgRegistrations()==null) {
Map<DashboardDuration, List<Map<String, Object>>> orgRegistrations = new HashMap<>();
stats.setOrgRegistrations(orgRegistrations);
}
stats.getOrgRegistrations().put(duration, orgDurationStatList);
}
});

List<DashboardDuration> monthlyDurations = Arrays.stream(DashboardDuration.values())
.filter(duration->{
if(duration.equals(DashboardDuration.SEMESTER)) return true;
if(duration.equals(DashboardDuration.THREE_MONTH)) return true;
if(duration.equals(DashboardDuration.YEAR)) return true;
return duration.equals(DashboardDuration.LAST_YEAR);
}).collect(Collectors.toList());

monthlyDurations.stream()
.forEach(duration->{
List<Map<String, Object>> churchDurationStatList = new ArrayList<>();
List<Map<String, Object>> orgDurationStatList = new ArrayList<>();
DateGap durationGap = DashboardUtils.getDateGap(duration, null);
LinkedList<DateGap> monthGaps = DateUtils.getMonthsGapsIn(durationGap.getFrom(), durationGap.getEnd(), duration);

// 'monthGaps' is already sorted chronologically by DateUtils.getMonthsGapsIn
// So, adding them in order to the list ensures the list is sorted.

monthGaps.forEach(month->{
String displayMonthName = DateUtils.getMonthName(month.getEnd());

if(institution == null || institution.getInstitutionType().equals(InstitutionType.CHURCH)){
churchRegistrationsQuery.setParameter("dateFrom", month.getFrom());
churchRegistrationsQuery.setParameter("dateEnd", month.getEnd());
Long churchRegistrations = churchRegistrationsQuery.getSingleResult();
churchRegistrations = churchRegistrations==null ? 0 : churchRegistrations;

Map<String, Object> entry = new LinkedHashMap<>();
entry.put("date", month.getEnd()); // Use Date object (end of month) for sorting
entry.put("displayDate", displayMonthName); // For frontend display (e.g., "Jul")
entry.put("value", churchRegistrations);
churchDurationStatList.add(entry);
}

if(institution == null || institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
orgRegistrationsQuery.setParameter("dateFrom", month.getFrom());
orgRegistrationsQuery.setParameter("dateEnd", month.getEnd());
Long organizationRegistrations = orgRegistrationsQuery.getSingleResult();
organizationRegistrations = organizationRegistrations==null ? 0 : organizationRegistrations;

Map<String, Object> entry = new LinkedHashMap<>();
entry.put("date", month.getEnd());
entry.put("displayDate", displayMonthName);
entry.put("value", organizationRegistrations);
orgDurationStatList.add(entry);
}
});

// After adding all entries, sort the list by the 'date' field
churchDurationStatList.sort(Comparator.comparing(m -> (Date)m.get("date")));
orgDurationStatList.sort(Comparator.comparing(m -> (Date)m.get("date")));

if(institution == null || institution.getInstitutionType().equals(InstitutionType.CHURCH)) {
// Type should be Map<DashboardDuration, List<Map<String, Object>>>
if(stats.getChurchRegistrations()==null) {
Map<DashboardDuration, List<Map<String, Object>>> churchRegistrations = new HashMap<>();
stats.setChurchRegistrations(churchRegistrations);
}
stats.getChurchRegistrations().put(duration, churchDurationStatList);
}

if(institution == null || institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
if(stats.getOrgRegistrations()==null) {
Map<DashboardDuration, List<Map<String, Object>>> orgRegistrations = new HashMap<>();
stats.setOrgRegistrations(orgRegistrations);
}
stats.getOrgRegistrations().put(duration, orgDurationStatList);
}
});
}
    @Transient
    @JsonIgnore
    public static void addGenderStats(@Nullable Institution institution,
                                      @NonNull EntityManager entityManager,
                                      @NonNull DashboardStats stats){
        try {
            String sql = "SELECT new com.dprince.apis.dashboard.vos.parts.GenderStat(" +
                    " SUM(CASE WHEN im.gender='MALE' THEN 1 ELSE 0 END) AS males," +
                    " SUM(CASE WHEN im.gender='FEMALE' THEN 1 ELSE 0 END) AS females" +
                    ")" +
                    " FROM InstitutionMember im" +
                    " INNER JOIN Institution i ON im.institutionId=i.id";
            if (institution != null) sql += " WHERE i.id=:institutionId";
            else sql += " WHERE i.institutionType=:institutionType";
            TypedQuery<GenderStat> query = entityManager.createQuery(sql, GenderStat.class);
            if (institution != null) {
                query.setParameter("institutionId", institution.getId());
                GenderStat generalStats = query.getSingleResult();
                stats.setMales(generalStats.getMales() == null ? 0 : generalStats.getMales());
                stats.setFemales(generalStats.getFemales() == null ? 0 : generalStats.getFemales());
            } else {
                query.setParameter("institutionType", InstitutionType.GENERAL);
                GenderStat generalStats = query.getSingleResult();
                stats.setOrganizationMales(generalStats.getMales() == null ? 0 : generalStats.getMales());
                stats.setOrganizationFemales(generalStats.getFemales() == null ? 0 : generalStats.getFemales());

                query.setParameter("institutionType", InstitutionType.CHURCH);
                GenderStat churchStats = query.getSingleResult();
                stats.setChurchMales(churchStats.getMales());
                stats.setChurchFemales(churchStats.getFemales());
            }
        } finally {
            entityManager.close();
        }
    }



    @JsonIgnore
    @Transient
    public static Page<InstitutionMember> getMembersBirthdays(@NotNull EntityManager entityManager,
                                                              @NonNull BirthdaysModel model){
        Page<InstitutionMember> foundResults;
        try {
            String sql = "SELECT im FROM InstitutionMember im",
                    whereQuery = " WHERE im.institutionId=:institutionId",
                    countSql = "SELECT COUNT(im) FROM InstitutionMember im";
            if (model.getEnd() == null) {
                switch (model.getBirthdayType()){
                    case BIRTHDAY:
                    default:
                        whereQuery += " AND CONVERT(DATE, FORMAT(im.dob, 'MMdd'))=CONVERT(DATE, FORMAT(:startDate, 'MMdd'))";
                        break;

                    case MARRIAGE:
                        whereQuery += " AND CONVERT(DATE, FORMAT(im.dom, 'MMdd'))=CONVERT(DATE, FORMAT(:startDate, 'MMdd'))";
                        break;
                }
            } else {
                switch (model.getBirthdayType()){
                    case BIRTHDAY:
                    default:
                        whereQuery += " AND CONVERT(DATE, FORMAT(im.dob, 'MMdd'))>=CONVERT(DATE, FORMAT(:startDate, 'MMdd')) AND CONVERT(DATE, FORMAT(im.dob, 'MMdd'))<=CONVERT(DATE, FORMAT(:endDate, 'MMdd'))";
                        break;

                    case MARRIAGE:
                        whereQuery += " AND CONVERT(DATE, FORMAT(im.dom, 'MMdd'))>=CONVERT(DATE, FORMAT(:startDate, 'MMdd')) AND CONVERT(DATE, FORMAT(im.dom, 'MMdd'))<=CONVERT(DATE, FORMAT(:endDate, 'MMdd'))";
                        break;
                }
            }
            TypedQuery<InstitutionMember> query = entityManager.createQuery(sql + whereQuery, InstitutionMember.class);
            TypedQuery<Long> countQuery = entityManager.createQuery(countSql + whereQuery, Long.class);
            query.setParameter("institutionId", model.getInstitutionId());
            countQuery.setParameter("institutionId", model.getInstitutionId());
            query.setParameter("startDate", model.getStart());
            countQuery.setParameter("startDate", model.getStart());
            if (model.getEnd() != null) {
                query.setParameter("endDate", model.getEnd());
                countQuery.setParameter("endDate", model.getEnd());
            }

            int page = model.getPage() - 1;
            if (page < 0) page = 0;
            query.setMaxResults(model.getSize());
            query.setFirstResult(page * model.getSize());
            List<InstitutionMember> foundMembers = query.getResultList();
            int foundMembersCount = Math.max(foundMembers.size(), 1);
            Long total = countQuery.getSingleResult();
            total = total==null ? 0 : total;
            foundResults = new PageImpl<>(foundMembers,
                    PageRequest.of(page, foundMembersCount),
                    total);
        } finally {
            entityManager.close();
        }
        return foundResults;
    }


    @Transient
    @JsonIgnore
    public InstitutionFamily getFamilyWhereHead(AppRepository appRepository){
        return appRepository.getInstitutionFamilyRepository()
                .findByFamilyHead(this.getId());
    }



    @JsonIgnore
    @Transient
    public boolean saveAsFamilyMemberOf(@NonNull AppRepository appRepository,
                                        @NonNull InstitutionFamily family){
        MemberOfFamilyTitle familyRole = this.getFamilyRole();
        // Save the new member
        InstitutionMember savedMember = (this.getId()==null)
                ? appRepository.getInstitutionMemberRepository().save(this)
                : this;
        savedMember.setFamilyRole(familyRole);
        family.addMember(savedMember);

        if(family.getMembers()!=null){
            family.getMembers()
                    .parallelStream()
                    .forEach(singleMember->{
                        InstitutionFamilyMember alreadySavedFamilyMember = appRepository
                                .getInstitutionFamilyMemberRepository()
                                .findByMemberIdAndFamilyId(singleMember.getId(), family.getId());

                        if(alreadySavedFamilyMember==null) {
                            this.saveFamilyMember(appRepository, family, singleMember);
                        }
                    });
            return true;
        }
        return false;
    }



    
    private void saveFamilyMember(@NonNull AppRepository appRepository,
                                  @NonNull InstitutionFamily family,
                                  @NonNull InstitutionMember singleMember){
        InstitutionFamilyMember familyMember = new InstitutionFamilyMember();
        familyMember.setMemberId(singleMember.getId());
        familyMember.setFamilyId(family.getId());
        familyMember.setTitle( (singleMember.getFamilyRole()!=null)
                ? singleMember.getFamilyRole()
                : MemberOfFamilyTitle.UNDEFINED);
        familyMember.setInstitutionId(singleMember.getInstitutionId());
        familyMember.setMemberSince(singleMember.getMemberSince()==null
                ? DateUtils.getNowDateTime()
                : singleMember.getMemberSince());

        boolean isAlreadySaved = familyMember.isAlreadySaved(appRepository);
        if(!isAlreadySaved) appRepository.getInstitutionFamilyMemberRepository().save(familyMember);
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean familyHead;

    @JsonIgnore
    @Transient
    private InstitutionReceipt receipt;
    private Map<String, String> getFillInData(){
        Map<String, String> data = new HashMap<>();
        if(!StringUtils.isEmpty(this.getFullName())) data.put("fullname", this.getFullName().toUpperCase());
        if(!StringUtils.isEmpty(this.getFirstName())) data.put("firstname", this.getFirstName().toUpperCase());
        if(!StringUtils.isEmpty(this.getLastName())) data.put("lastname", this.getLastName().toUpperCase());
        if(!StringUtils.isEmpty(this.getCode())) data.put("membercode", this.getCode());
        if(!StringUtils.isEmpty(this.getFullAddress())) data.put("memberaddress", this.getFullAddress().toUpperCase());

        if(this.getInstitution()!=null) {
            String theAddress = this.getInstitution().getAddress(),
                    bibleVerse = this.getInstitution().getBibleVerse(),
                    website = this.getInstitution().getWebsite(),
                    institutionName = this.getInstitution().getName();
            data.put("bibleverse", StringUtils.isEmpty(bibleVerse) ? "BIBLE" : bibleVerse.toUpperCase());
            data.put("theaddress", StringUtils.isEmpty(theAddress) ? "Our Address" : theAddress.toUpperCase());
            data.put("thewebsite", StringUtils.isEmpty(website) ? "https://dnote.ai" : website);
            data.put("thephone", this.getInstitution().getPhone()+"");
            data.put("institution", institutionName.toUpperCase());
        }
        data.put("memberemail", StringUtils.isEmpty(this.getEmail()) ? "" : this.getEmail());

        if(this.getReceipt()!=null){
            data.put("receiptpayment", this.getReceipt().getPaymentMode().getDisplayName());
            if(this.getReceipt().getDonations()!=null && !this.getReceipt().getDonations().isEmpty()){
                Long totalAmount = this.getReceipt().getTotal();
                data.put("receiptamount", totalAmount.toString());
                data.put("amount", totalAmount.toString());
                data.put("receiptno", this.getReceipt().getReceiptNo());
                data.put("receiptdate", DateUtils.getDateString(this.getReceipt().getEntryDate()));
                data.put("date", DateUtils.getDateString(this.getReceipt().getEntryDate()));

                if(this.getReceipt().getPaidSubscriptions()!=null
                        && !this.getReceipt().getPaidSubscriptions().isEmpty()) {
                    String paidSubscriptionsString = String.join(", ", this.getReceipt().getPaidSubscriptions());
                    data.put("subscriptions", paidSubscriptionsString.toUpperCase());
                }
            }
        }
        return data;
    }


    @JsonIgnore
    public boolean sendWhatsappMessage(@NonNull WhatsappMessageConfig config){
        boolean sent = false;
        Notification notification;
        NotificationActionType notificationType = NotificationActionType.DELETE;
        String title = (!StringUtils.isEmpty(config.getMessageTitle()))
                ? config.getMessageTitle()
                : "Whatsapp message",
                messageString = this.getFullName()+" details do not include Whatsapp phone";
        if(!StringUtils.isEmpty(this.getFullWhatsappFullNumber())) {
            // If the template is other the use the template selection
            Template template = config.getTemplate();
            if(template==null) {
                if(config.getTemplateId()!=null){
                    template = config.getAppRepository().getTemplateRepository()
                            .findById(config.getTemplateId()).orElse(null);
                } else {
                    template = config.getAppRepository().getTemplateRepository()
                            .findFirstByInstitutionIdAndTemplateStyleOrderByIdDesc(
                                    this.getInstitutionId(),
                                    config.getTemplateStyle());
                }
            }

            // Once the settings have been done and successful
            if (template != null) {
                WhatsappMessage message = new WhatsappMessage(
                        this.getWhatsappNumberCode(),
                        this.getWhatsappNumber());
                WhatsappMessageContext context = new WhatsappMessageContext(template.getWhatsappTemplate());
                context.setLanguage(config.getLanguage());
                WhatsappMessageData data = new WhatsappMessageData();
                data.setType(WhatsappMessageType.template);
                data.setContext(context);
                data.setLanguage(config.getLanguage());
                // Fill the body with variables
                message.setFillInData(this.getFillInData());
                message.setData(data);
                message.setInstitution(institution);
                WhatsappApiResponse response = config.getWhatsappService().sendMessage(message);
                if(response!=null) {
                    if (response.getCode().equals("200")) {
                        // Notify that this message has been used.
                        messageString = "Message sent to: " + this.getFullName()+".";
                        notificationType = NotificationActionType.CREATE;


                        if(this.getReceipt()!=null) {
                            ReceiptAcknowledgement acknowledgement = new ReceiptAcknowledgement();
                            acknowledgement.setCommunicationWay(CommunicationWay.WHATSAPP);
                            acknowledgement.setReceiptId(this.getReceipt().getId());
                            acknowledgement.setInstitutionId(this.getInstitutionId());
                            if (config.getSender() != null) acknowledgement.setSenderId(config.getSender().getId());
                            config.getAppRepository().getReceiptAcknowledgementRepository()
                                    .save(acknowledgement);
                        }
                        sent = true;
                    } else {
                        // Notify the message has not been used.
                        messageString = "Failed to send message to " + this.getFullName().toUpperCase()+".";
                    }
                } else {
                    messageString = "We have not been able to send your whatsapp message. Please contact the admiistrator.";
                    sent = false;
                }
            }
        }

        notification = Notification.createNotification(null,
                title,
                messageString,
                notificationType,
                this.getInstitutionId());
        config.getAppRepository().getNotificationsRepository().save(notification);
        return sent;
    }


    public void subscribe(@NonNull AppRepository appRepository,
                          @NonNull InstitutionMember member,
                          @NonNull InstitutionReceipt receipt,
                          @Nullable Subscription subscription){
        DonationContribution contribution = new DonationContribution();
        contribution.setMemberId(member.getId());
        contribution.setInstitutionId(member.getInstitutionId());
        contribution.setPaymentMode(receipt.getPaymentMode());
        contribution.setReferenceAccount(receipt.getReferenceAccount());
        contribution.setReferenceNo(receipt.getReferenceNo());
        contribution.setRemarks(receipt.getRemarks());
        contribution.setBankReference(receipt.getBankReference());
        contribution.setCreditAccount(receipt.getCreditAccountId());

        contribution.addDonation(receipt.getDonations(), subscription);

        this.subscribe(appRepository, member.getInstitution(), contribution);
    }

    /**
     * @description: <p>
     *     Do not call this when attempting to make donation from a church.
     *     This method should only be called when a donation is made from
     *      a General Organization.
     * </p>
     * @param appRepository: An Instance of the AppRepository class
     * @param institution: Institution within which this subscription is being made
     * @param contribution: An Instance of the DonationContribution class
     */
    public void subscribe(@NonNull AppRepository appRepository,
                          @NonNull Institution institution,
                          @NonNull DonationContribution contribution){
        Date startDate = DateUtils.getNowDateTime();
        Date today = DateUtils.getNowDateTime();

        List<PeopleSubscription> subscriptions = new ArrayList<>();
        contribution.getDonations()
                .forEach(singleDonation->{
                    PeopleSubscription peopleSubscription = institution.getInstitutionType().equals(InstitutionType.CHURCH)
                            ? appRepository.getPeopleSubscriptionRepository()
                            .findFirstByMemberIdAndContributionIdOrderByIdDesc(this.getId(), singleDonation.getContributionId()).orElse(null)
                            : appRepository.getPeopleSubscriptionRepository()
                            .findFirstByMemberIdAndCategoryIdOrderByIdDesc(this.getId(), singleDonation.getCategoryId()).orElse(null);
                    if(peopleSubscription!=null && peopleSubscription.getDeadline().after(today)) {
                        deadline = peopleSubscription.getDeadline();
                    } else {
                        peopleSubscription = new PeopleSubscription();
                        peopleSubscription.setDeadline(startDate);
                        peopleSubscription.setMemberId(this.getId());
                        peopleSubscription.setInstitutionId(this.institutionId);
                        peopleSubscription.setSubscription(singleDonation.getSubscription());
                        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
                            peopleSubscription.setContributionId(singleDonation.getContributionId());
                            if(this.getContributionsIds()==null || !this.getContributionsIds().contains(singleDonation.getCategoryId())) {
                                this.addContribution(singleDonation.getCategoryId());
                            }
                        } else {
                            peopleSubscription.setCategoryId(singleDonation.getCategoryId());
                            if(this.getCategoriesIds()==null || !this.getCategoriesIds().contains(singleDonation.getCategoryId())) {
                                this.addCategory(singleDonation.getCategoryId());
                            }
                        }
                    }
                    peopleSubscription.setStart(startDate);
                    peopleSubscription.computeDeadline(singleDonation.getSubscription());
                    subscriptions.add(peopleSubscription);
                });
        appRepository.getPeopleSubscriptionRepository().saveAll(subscriptions);
        appRepository.getInstitutionMemberRepository().save(this);
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String signature;


    /**
     * This method will be temporarily called. Once every thing is fine then we can omit it
     */
    public PeopleSubscription correctSubscriptions(@NonNull AppRepository appRepository,
                                     @NonNull Institution institution,
                                     long paymentForId){
        Subscription userSubscription = this.getSubscription()==null
                ? Subscription.ANNUAL
                : this.getSubscription();
        PeopleSubscription subscription;
        this.setInstitution(institution);
        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            InstitutionDonation donation = new InstitutionDonation();
            donation.setEntryDate(this.getCreationDate());
            donation.setChurchContributionId(paymentForId);
            subscription = PeopleSubscription.makeSubscription(this,
                    userSubscription,
                    donation);
        } else {
            InstitutionDonation donation = new InstitutionDonation();
            donation.setEntryDate(this.getCreationDate());
            donation.setCategoryId(paymentForId);
            subscription = PeopleSubscription.makeSubscription(this,
                    userSubscription,
                    donation);
        }
        return appRepository.getPeopleSubscriptionRepository().save(subscription);
    }
}
