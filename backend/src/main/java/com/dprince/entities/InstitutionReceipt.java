package com.dprince.entities;

import com.dprince.apis.institution.models.AddressRequestModel;
import com.dprince.apis.institution.models.DonationListQuery;
import com.dprince.apis.institution.models.enums.DurationGap;
import com.dprince.apis.institution.vos.PartnerDonationVO;
import com.dprince.apis.institution.vos.Receiptor;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.Messenger;
import com.dprince.apis.utils.vos.InstitutionSimpleVO;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.PaymentMode;
import com.dprince.entities.enums.TemplateStyle;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.utils.QueryHelper;
import com.dprince.entities.vos.DonationReport;
import com.dprince.startup.GeneralValues;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.dprince.apis.institution.models.enums.DurationGap.LAST_SIX_MONTH;
import static com.dprince.apis.institution.models.enums.DurationGap.SINGLE_DATE;

@FieldNameConstants
@Data
@Entity
@Table(name = "institutions_receipts",
        indexes = {
            @Index(name = "idx_payment_mode", columnList = "paymentMode"),
})
public class InstitutionReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long categoryId;


        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private InstitutionCategory category;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private ChurchContribution contribution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;



    private Long creditAccountId;
        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private InstitutionCreditAccount creditAccount;


    @Temporal(TemporalType.TIMESTAMP)
    private Date entryDate = DateUtils.getNowDateTime();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private Long institutionId;
        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private InstitutionSimpleVO institution;

        @Transient
        @JsonIgnore
        private Institution institutionOriginal;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Column(nullable = false)
        private Long memberId;

    @Column(nullable = false, length = 30)
    private String receiptNo;


    private String remarks;
    public void setRemarks(String remarks) {
        if(!StringUtils.isEmpty(remarks)) remarks = remarks.trim();
        this.remarks = remarks;
    }

    @Column(length = 32)
    private String referenceNo;
    public void setReferenceNo(String referenceNo) {
        if(!StringUtils.isEmpty(referenceNo)) referenceNo = referenceNo.trim();
        this.referenceNo = referenceNo;
    }

    @Column(length=40)
    private String bankReference;
    public void setBankReference(String bankReference) {
        if(!StringUtils.isEmpty(bankReference)) bankReference = bankReference.trim();
        this.bankReference = bankReference;
    }

    @Column(length = 40)
    private String referenceAccount;
    public void setReferenceAccount(String referenceAccount) {
        if(!StringUtils.isEmpty(referenceAccount)) referenceAccount = referenceAccount.trim();
        this.referenceAccount = referenceAccount;
    }


        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private List<InstitutionDonation> donations;
            public void addDonation(InstitutionDonation donation){
                if(this.donations==null) this.donations = new ArrayList<>();
                this.donations.add(donation);
            }

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private InstitutionMember member;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Transient
    private List<ReceiptAcknowledgement> lastAcknowledgements;


    @Transient
    @JsonIgnore
    private ObjectMapper objectMapper;

    @JsonIgnore
    @Transient
    private Map<Long, InstitutionCategory> categoriesMap = null;

    @JsonIgnore
    @Transient
    private Map<Long, ChurchContribution> churchContributionsMap = null;
    public void computeDonationsMaps(@NonNull AppRepository appRepository,
                                     @NonNull Institution institution){
        if(institution.isChurch()) {
            Map<Long, ChurchContribution> churchContributions = appRepository.getChurchContributionRepository()
                    .findAllByInstitutionId(institution.getId())
                    .parallelStream()
                    .collect(Collectors.toMap(ChurchContribution::getId, contribution -> contribution));
            this.setChurchContributionsMap(churchContributions);
        } else {
            Map<Long, InstitutionCategory> categories = appRepository.getCategoryRepository()
                    .findAllByInstitutionId(institution.getId() )
                    .parallelStream()
                    .collect(Collectors.toMap(InstitutionCategory::getId, category -> category));
            this.setCategoriesMap(categories);
        }
    }


    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<String> donationTargets = new HashSet<>();
    private void addDonationTarget(String name){
        if(!StringUtils.isEmpty(name)) this.donationTargets.add(name);
    }

    public void populate(@NonNull AppRepository appRepository,
                         boolean addInstitution){
        if(addInstitution) this.addInstitution(appRepository);
        boolean computeDonations = false;
        if(this.getInstitutionOriginal().isChurch()){
            if(this.getChurchContributionsMap()==null) computeDonations = true;
        } else {
            if(this.getCategoriesMap()==null) computeDonations = true;
        }

        if(computeDonations) this.computeDonationsMaps(appRepository, this.getInstitutionOriginal());
        List<InstitutionDonation> foundDonations = appRepository
                .getInstitutionDonationRepository()
                .findAllByReceiptNoAndInstitutionId(this.getReceiptNo(), this.getInstitutionId());

        List<InstitutionDonation> uniqueDonations = foundDonations
                .parallelStream()
                        .collect(Collectors.toMap(obj->obj,
                                obj->obj,
                                (existing, replacement)->existing))
                                .values()
                                        .parallelStream()
                                                .collect(Collectors.toList());
        uniqueDonations.parallelStream().forEach(donation-> {
            if(this.getInstitutionOriginal().getInstitutionType().equals(InstitutionType.GENERAL)){
                if(this.getCategoriesMap()!=null) {
                    if(donation.getCategoryId()!=null) {
                        InstitutionCategory theCategory = this.getCategoriesMap().get(donation.getCategoryId());
                        if (theCategory != null) {
                            donation.setCategory(theCategory);
                            this.addDonationTarget(theCategory.getName());
                        }
                    }
                } else {
                    donation.populate(appRepository, this.getInstitutionOriginal());
                }
            } else {
                if(this.getChurchContributionsMap()!=null){
                    if(donation.getChurchContributionId()!=null){
                        ChurchContribution theContribution = this.getChurchContributionsMap().get(donation.getChurchContributionId());
                        if(theContribution != null) {
                            donation.setContribution(theContribution);
                            this.addDonationTarget(theContribution.getName());
                        }
                    }
                } else {
                    donation.populate(appRepository, this.getInstitutionOriginal());
                }
            }
        });
        this.setDonations(uniqueDonations);

        if(this.getMember()==null) {
            appRepository.getInstitutionMemberRepository().
                    findById(this.getMemberId())
                    .ifPresent(owner->{
                        PersonTitle personTitle = GeneralValues.PERSON_TITLES.get(owner.getTitleId());
                        if(personTitle!=null) owner.setTitle(personTitle.getShortName());
                        this.setMember(owner);
                    });
        }

        if(this.getCreditAccountId()!=null){
            appRepository.getCreditAccountsRepository()
                    .findById(this.getCreditAccountId())
                    .ifPresent(this::setCreditAccount);
        }

        // Get last sent sms
        List<ReceiptAcknowledgement> lastSentSms = new ArrayList<>();
        for(CommunicationWay way : CommunicationWay.values()){
            ReceiptAcknowledgement last = appRepository
                    .getReceiptAcknowledgementRepository()
                    .findFirstByReceiptIdAndCommunicationWayOrderByMessageSendingTimeDesc(this.getId(), way);
            if(last!=null) lastSentSms.add(last);
        }
        this.setLastAcknowledgements(lastSentSms);
    }

    public void addInstitution(AppRepository appRepository){
        if(this.getInstitutionId()!=null) {
            appRepository.getInstitutionRepository().findById(this.getInstitutionId()).ifPresent(savedInstitution -> {
                if (this.objectMapper != null) {
                    InstitutionSimpleVO vo = this.objectMapper
                            .convertValue(savedInstitution,
                                    InstitutionSimpleVO.class);
                    this.setInstitution(vo);
                }
                this.setInstitutionOriginal(savedInstitution);
            });
        }
    }

    public void delete(AppRepository appRepository){
        appRepository.getInstitutionDonationRepository()
                .deleteAllByReceiptNo(this.getReceiptNo());
        if(this.getId()!=null) appRepository.getInstitutionReceiptsRepository().deleteById(this.getId());
    }



    @Transient
    @JsonIgnore
    public static PartnerDonationVO getMemberReceipts(@NotNull EntityManager entityManager,
                                                      @NotNull AppRepository appRepository,
                                                      DonationListQuery donationListQuery,
                                                      @NonNull InstitutionSimpleVO institutionVO){
        PartnerDonationVO partnerDonationVO;
        try {
            String selectSql = "SELECT DISTINCT re FROM InstitutionReceipt re",
                    countSql = "SELECT COUNT(*) FROM InstitutionReceipt re",
                    sumSql = "SELECT SUM(don.amount) FROM InstitutionReceipt re",
                    conditionString = " INNER JOIN InstitutionDonation don ON re.receiptNo=don.receiptNo";

            conditionString += " WHERE don.institutionId=:institutionId";

            if (donationListQuery.getMemberId() != null) conditionString += " AND re.memberId=:memberId";
            if (!StringUtils.isEmpty(donationListQuery.getReceiptNo()))
                conditionString += " AND re.receiptNo=:receiptNo";
            if (donationListQuery.getCategory() != null) {
                if (donationListQuery.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)) {
                    conditionString += " AND don.categoryId=:categoryId";
                } else {
                    conditionString += " AND don.churchContributionId=:categoryId";
                }
            }
            if (donationListQuery.getPaymentMode() != null) conditionString += " AND re.paymentMode=:paymentMode";
            if (donationListQuery.getFrom() != null) {
                conditionString += " AND CONVERT(DATE, re.entryDate)>=CONVERT(DATE, :fromDate)";
            }
            if (donationListQuery.getTo() != null) {
                conditionString += " AND CONVERT(DATE, re.entryDate)<=CONVERT(DATE, :toDate)";
            }

            int page = donationListQuery.getPage() - 1;
            if (page < 0) page = 0;
            int offset = page * donationListQuery.getSize();


            TypedQuery<InstitutionReceipt> receiptTypedQuery = entityManager.createQuery(
                    selectSql + conditionString + " ORDER BY re.entryDate DESC",
                    InstitutionReceipt.class);
            TypedQuery<Long> countTypedQuery = entityManager.createQuery(
                    countSql + conditionString,
                    Long.class);
            TypedQuery<Long> sumTypedQuery = entityManager.createQuery(
                    sumSql + conditionString,
                    Long.class);

            receiptTypedQuery.setParameter("institutionId", donationListQuery.getInstitutionId());
            countTypedQuery.setParameter("institutionId", donationListQuery.getInstitutionId());
            sumTypedQuery.setParameter("institutionId", donationListQuery.getInstitutionId());

            if (donationListQuery.getMemberId() != null) {
                receiptTypedQuery.setParameter("memberId", donationListQuery.getMemberId());
                countTypedQuery.setParameter("memberId", donationListQuery.getMemberId());
                sumTypedQuery.setParameter("memberId", donationListQuery.getMemberId());
            }

            if (!StringUtils.isEmpty(donationListQuery.getReceiptNo())) {
                receiptTypedQuery.setParameter("receiptNo", donationListQuery.getReceiptNo());
                countTypedQuery.setParameter("receiptNo", donationListQuery.getReceiptNo());
                sumTypedQuery.setParameter("receiptNo", donationListQuery.getReceiptNo());
            }

            if (donationListQuery.getCategory() != null) {
                receiptTypedQuery.setParameter("categoryId", donationListQuery.getCategory());
                countTypedQuery.setParameter("categoryId", donationListQuery.getCategory());
                sumTypedQuery.setParameter("categoryId", donationListQuery.getCategory());
            }

            if (donationListQuery.getPaymentMode() != null) {
                receiptTypedQuery.setParameter("paymentMode", donationListQuery.getPaymentMode());
                countTypedQuery.setParameter("paymentMode", donationListQuery.getPaymentMode());
                sumTypedQuery.setParameter("paymentMode", donationListQuery.getPaymentMode());
            }

            if (donationListQuery.getFrom() != null) {
                receiptTypedQuery.setParameter("fromDate", donationListQuery.getFrom());
                countTypedQuery.setParameter("fromDate", donationListQuery.getFrom());
                sumTypedQuery.setParameter("fromDate", donationListQuery.getFrom());
            }

            if (donationListQuery.getTo() != null) {
                receiptTypedQuery.setParameter("toDate", donationListQuery.getTo());
                countTypedQuery.setParameter("toDate", donationListQuery.getTo());
                sumTypedQuery.setParameter("toDate", donationListQuery.getTo());
            }

            // Set pagination parameters
            receiptTypedQuery.setFirstResult(page * donationListQuery.getSize());
            receiptTypedQuery.setMaxResults(donationListQuery.getSize());
            List<InstitutionReceipt> results = receiptTypedQuery.getResultList();

            Long totalResult = countTypedQuery.getSingleResult();
            if (totalResult == null) totalResult = 0L;

            // AtomicReference<Long> totalDonations = new AtomicReference<>(sumTypedQuery.getSingleResult());
            // if (totalDonations.get() == null) totalDonations.set(0L);
            AtomicReference<Long> totalDonations = new AtomicReference<>(0L);

            if (!results.isEmpty()) {
                InstitutionMember member = appRepository.getInstitutionMemberRepository()
                        .findById(results.get(0).getMemberId())
                        .orElse(null);
                if (member != null) {
                    member.setInstitution(donationListQuery.getInstitution());
                    InstitutionReceipt.populateReceipts(appRepository, member,
                            results, institutionVO);
                } else {
                    totalResult = 0L;
                    totalDonations.set(0L);
                }

                results.parallelStream()
                        .forEach(singleReceipt->{
                            long theReceiptTotal = singleReceipt.getTotal();
                            totalDonations.updateAndGet(v -> v + theReceiptTotal);
                        });
            } else {
                totalResult = 0L;
                totalDonations.set(0L);
            }

            Page<InstitutionReceipt> pagedResult = new PageImpl<>(results,
                    PageRequest.of(page, donationListQuery.getSize()),
                    totalResult);
            partnerDonationVO = PartnerDonationVO.builder()
                    .receiptsOriginal(pagedResult)
                    .total(totalDonations.get())
                    .build();
        } catch (Exception e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to load Receipts...");
        }
        return partnerDonationVO;
    }

    private void compute(){

    }
    public static void populateReceipts(AppRepository appRepository,
                                        @NotNull InstitutionMember member,
                                        List<InstitutionReceipt> results,
                                        @NonNull InstitutionSimpleVO institutionVO){
        Map<Long, ChurchContribution> churchContributions = appRepository.getChurchContributionRepository()
                .findAllByInstitutionId(member.getInstitutionId())
                .parallelStream()
                .collect(Collectors.toMap(ChurchContribution::getId, contribution->contribution));

        Map<Long, InstitutionCategory> categories = appRepository.getCategoryRepository()
                .findAllByInstitutionId(member.getInstitutionId())
                .parallelStream()
                .collect(Collectors.toMap(InstitutionCategory::getId, category->category));

        results.parallelStream().forEach(singleReceipt->{
            if(member.getInstitution()!=null) {
                singleReceipt.setInstitutionOriginal(member.getInstitution());
                if(member.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)) singleReceipt.setCategoriesMap(categories);
                else singleReceipt.setChurchContributionsMap(churchContributions);
                singleReceipt.populate(appRepository, true);
                singleReceipt.setInstitution(institutionVO);
            }
        });
    }


    private static QueryHelper compu(AddressRequestModel listQuery,
                                     Institution institution,
                                     boolean all){
        QueryHelper queryHelper = new QueryHelper();
        List<String> queries = new ArrayList<>();
        if (listQuery.getCreditAccountId() != null) {
            queryHelper.setCommonPart((all ? " LEFT" : " INNER") + " JOIN InstitutionCreditAccount ica ON im.institutionId=ica.institutionId");
            queries.add("re.creditAccountId=:creditAccount");
        }

        if (listQuery.getMinAmount() != null || listQuery.getMaxAmount() != null) {
            if (listQuery.getMinAmount() != null) queries.add("isd.amount>=:minAmount");
            if (listQuery.getMaxAmount() != null) queries.add("isd.amount<=:maxAmount");
        }

        if (listQuery.getPaymentMode() != null && !listQuery.getPaymentMode().isEmpty()) {
            queries.add("re.paymentMode IN (:paymentModes)");
        }

        if (listQuery.getCategories() != null && !listQuery.getCategories().isEmpty()) {
            if (institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                // commonPart += " INNER JOIN PeopleToCategory ptc ON im.id=ptc.memberId";
                queries.add("isd.categoryId IN (:categoriesList)");
            } else {
                queries.add("isd.churchContributionId IN (:categoriesList)");
            }
            queries.add("isd.id IS NOT NULL");
        }
        if(!queries.isEmpty()) queryHelper.setQuery(String.join(" AND ", queries));
        return queryHelper;
    }


    private static String addCategoryString(AddressRequestModel listQuery,
                                            Institution institution){
        List<String> categoriesString = new ArrayList<>();
        String output = "";
        if(listQuery.getCategories()!=null && !listQuery.getCategories().isEmpty()){
            listQuery.getCategories().forEach(categoryId->{
                if (institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                    categoriesString.add("CONCAT(',',im.categoriesIds,',') LIKE '%," + categoryId + ",%'");
                } else {
                    categoriesString.add("CONCAT(',',im.contributionsIds,',') LIKE '%," + categoryId + ",%'");
                }
            });
        }
        output = "("+String.join(" OR ", categoriesString)+")";
        return output;
    }

    private static String getCountryBasedQuery(AddressRequestModel listQuery){
        List<String> queries = new ArrayList<>();
        if (listQuery.getCountry()!=null && !listQuery.getCountry().isEmpty()) queries.add("im.country IN :country");
        if(!StringUtils.isEmpty(listQuery.getDistrict())) queries.add("LOWER(im.district)=LOWER(:district)");
        if (listQuery.getPhone() != null) queries.add("CONCAT(CONVERT(VARCHAR, im.phoneCode), CONVERT(VARCHAR, im.phone))=CONVERT(VARCHAR, :phone)");
        if (listQuery.getAlternatePhone() != null) queries.add("CONCAT(CONVERT(VARCHAR, im.alternatePhoneCode), CONVERT(VARCHAR, im.alternatePhone))=CONVERT(VARCHAR, :alternatePhone)");

        if (listQuery.getLanguages() != null && !listQuery.getLanguages().isEmpty()) {
            queries.add("im.language IN :languagesList");
        }
        if (listQuery.getPincode() != null) queries.add("im.pincode=:pincode");
        return String.join(" AND ", queries);
    }

    @Transient
    @JsonIgnore
    public static Receiptor getReport(EntityManager entityManager,
                                                 AppRepository appRepository,
                                                 AddressRequestModel listQuery,
                                                 Institution institution){
        Receiptor receiptor;
        try {
            String commonPart = " FROM InstitutionMember im",
                    selectQuery = "SELECT DISTINCT new com.dprince.entities.vos.DonationReport(" +
                            "im.id as id, "+
                            "im.phoneCode as phoneCode, "+
                            "im.phone as phone, "+
                            "im.code as memberCode, " +
                            "concat(im.firstName, ' ', im.lastName) as donator, " +
                            "im.gender as gender)";
            if(listQuery.getWithReceipt()!=null){
                if(listQuery.getWithReceipt()) {
                    commonPart += " INNER JOIN InstitutionDonation isd ON isd.memberId=im.id AND isd.institutionId=im.institutionId";
                    commonPart += " INNER JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo AND re.institutionId=im.institutionId";

                    selectQuery = "SELECT DISTINCT new com.dprince.entities.vos.DonationReport(" +
                                "re.id as id, "+
                                "re.receiptNo as receiptNo, "+
                                "re.entryDate as donationDate, "+
                                "im.code as memberCode, " +
                                "concat(im.firstName, ' ', im.lastName) as donator, " +
                                (
                                        (institution.getInstitutionType().equals(InstitutionType.GENERAL))
                                                ? "ic.name as category, "
                                                : "cc.name as category, "
                                ) +
                                "re.paymentMode as paymentMode, "+
                                "isd.amount as amount)";
                } else {
                    commonPart += " LEFT JOIN InstitutionDonation isd ON isd.memberId=im.id AND isd.institutionId=im.institutionId";
                    // commonPart += " LEFT JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo";
                }
            } else {
                commonPart += " LEFT JOIN InstitutionDonation isd ON isd.memberId=im.id AND isd.institutionId=im.institutionId";
                // commonPart += " LEFT JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo";
            }
            /*
            if(listQuery.getWithReceipt()!=null){
                commonPart += (listQuery.getWithReceipt()
                        ? " INNER JOIN InstitutionDonation isd"
                        : " LEFT JOIN InstitutionDonation isd"
                ) +
                        " ON isd.memberId=im.id AND isd.institutionId=im.institutionId" +
                        (listQuery.getWithReceipt()
                                ? " INNER JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo AND re.institutionId=im.institutionId"
                                : " LEFT JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo"
                        );
                selectQuery = listQuery.getWithReceipt()
                        ? "SELECT DISTINCT new com.dprince.entities.vos.DonationReport(" +
                            "re.id as id, "+
                            "re.receiptNo as receiptNo, "+
                            "re.entryDate as donationDate, "+
                            "im.code as memberCode, " +
                            "concat(im.firstName, ' ', im.lastName) as donator, " +
                            (
                                    (institution.getInstitutionType().equals(InstitutionType.GENERAL))
                                            ? "ic.name as category, "
                                            : "cc.name as category, "
                            ) +
                            "re.paymentMode as paymentMode, "+
                            "isd.amount as amount)"
                        :  "SELECT DISTINCT new com.dprince.entities.vos.DonationReport(" +
                            "im.id as id, "+
                            "im.creationDate as donationDate, "+
                            "im.code as memberCode, " +
                            "concat(im.firstName, ' ', im.lastName) as donator, " +
                            "im.gender as gender)";
            } else {
                commonPart += " LEFT JOIN InstitutionDonation isd ON isd.memberId=im.id AND isd.institutionId=im.institutionId";
                commonPart += " LEFT JOIN InstitutionReceipt re ON re.receiptNo=isd.receiptNo AND re.memberId IS NULL";
            }
             */

            String countQuery = "SELECT COUNT(*) ",
                    sumQuery = (listQuery.getWithReceipt()!=null && listQuery.getWithReceipt())
                            ? "SELECT SUM(isd.amount) "
                            : null;

            String whereQuery = " WHERE (im.institutionId=:institutionId";
            if(listQuery.getWithReceipt()!=null && listQuery.getWithReceipt()){
                commonPart += institution.getInstitutionType().equals(InstitutionType.GENERAL)
                        ? " INNER JOIN InstitutionCategory ic ON ic.id=isd.categoryId"
                        : " INNER JOIN ChurchContribution cc ON cc.id=isd.churchContributionId";
                whereQuery += institution.getInstitutionType().equals(InstitutionType.GENERAL)
                        ? " AND ic.institutionId=:institutionId"
                        : " AND cc.institutionId=:institutionId";
                whereQuery += " AND re.institutionId=:institutionId AND isd.institutionId=:institutionId";
            }
            whereQuery += ")";

            if(listQuery.getActive()!=null) whereQuery += " AND im.active=:memberActive";
            if (!StringUtils.isEmpty(listQuery.getQuery())) {
                whereQuery += " AND (LOWER(im.firstName) LIKE LOWER(:query) OR LOWER(im.lastName) LIKE LOWER(:query) OR LOWER(CONCAT(im.firstName, ' ', im.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(im.lastName, ' ', im.firstName)) LIKE LOWER(:query))";
            }

            String countryBasedQuery = InstitutionReceipt.getCountryBasedQuery(listQuery);
            if(!StringUtils.isEmpty(countryBasedQuery)) whereQuery += " AND "+countryBasedQuery;


            if(listQuery.getWithReceipt()!=null) {
                if(listQuery.getWithReceipt()) {
                    QueryHelper helper = InstitutionReceipt.compu(listQuery, institution, false);
                    if(!StringUtils.isEmpty(helper.getQuery())) whereQuery += " AND "+helper.getQuery();
                    if(!StringUtils.isEmpty(helper.getCommonPart())) commonPart += helper.getCommonPart();
                } else {
                    if(listQuery.getCategories()!=null && !listQuery.getCategories().isEmpty()) {
                        String categoryQuery = InstitutionReceipt.addCategoryString(listQuery, institution);
                        if (!StringUtils.isEmpty(categoryQuery)) whereQuery += " AND " + categoryQuery;
                    }
                    whereQuery += " AND isd.id IS NULL";
                }
            } else {
                if(listQuery.getCategories()!=null && !listQuery.getCategories().isEmpty()) {
                    List<String> queries = new ArrayList<>();

                    QueryHelper helper = InstitutionReceipt.compu(listQuery, institution, true);
                    if(!StringUtils.isEmpty(helper.getQuery())) queries.add("("+helper.getQuery()+")");
                    if(!StringUtils.isEmpty(helper.getCommonPart())) commonPart += helper.getCommonPart();


                    String categoryQueryString = InstitutionReceipt.addCategoryString(listQuery, institution);
                    if (!StringUtils.isEmpty(categoryQueryString)) queries.add("("+categoryQueryString+")");
                    if(!queries.isEmpty()) whereQuery += " AND ("+String.join(" OR ", queries)+")";
                }
                if(!StringUtils.isEmpty(countryBasedQuery)) whereQuery += " AND " + countryBasedQuery;
            }

            switch (listQuery.getDuration()) {
                case TODAY:
                case SINGLE_DATE:
                    if(listQuery.getWithReceipt()!=null) {
                        if(listQuery.getWithReceipt()){
                            whereQuery += " AND (CONVERT(DATE, re.entryDate)=CONVERT(DATE, :fromDate)";
                            whereQuery += " AND isd.institutionId=:institutionId";
                        } else {
                            whereQuery += " AND (CONVERT(DATE, im.creationDate)=CONVERT(DATE, :fromDate)";
                        }
                        whereQuery += " AND im.institutionId=:institutionId)";
                    } else {
                        // And
                        whereQuery += " AND ((CONVERT(DATE, isd.entryDate)=CONVERT(DATE, :fromDate)";
                        whereQuery += " AND isd.institutionId=:institutionId)";

                        // OR
                        whereQuery += " OR CONVERT(DATE, im.creationDate)=CONVERT(DATE, :fromDate))";

                        whereQuery += " AND im.institutionId=:institutionId";
                    }
                    break;

                case LAST_SEVEN_DAYS:
                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                case YEAR:
                case CUSTOM_DATE_GAP:
                    if(listQuery.getWithReceipt()!=null) {
                        if(listQuery.getWithReceipt()){
                            whereQuery += " AND ((CONVERT(DATE, re.entryDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                            whereQuery += " AND isd.institutionId=:institutionId";
                        } else{
                            whereQuery += " AND ((CONVERT(DATE, im.creationDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        }
                        whereQuery += " AND im.institutionId=:institutionId)";
                    } else {
                        // AND
                        whereQuery += " AND ((((CONVERT(DATE, isd.entryDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate))";
                        whereQuery += " AND isd.institutionId=:institutionId)";

                        // OR
                        whereQuery += " OR (CONVERT(DATE, im.creationDate) BETWEEN CONVERT(DATE, :fromDate) AND CONVERT(DATE, :toDate)))";

                        whereQuery += " AND im.institutionId=:institutionId)";
                    }
                    break;
            }

            if (listQuery.getSubscription() != null && !listQuery.getSubscription().isEmpty()){
                commonPart += " INNER JOIN PeopleSubscription psubs ON psubs.memberId=im.id";
                whereQuery += " AND psubs.subscription IN (:subscription)";
            }

            if (listQuery.getStates() != null && !listQuery.getStates().isEmpty()) {
                whereQuery += " AND im.state IN (:addressStates)";
            } else {
                if (!StringUtils.isEmpty(listQuery.getState())) {
                    whereQuery += " AND im.state=:addressStates";
                }
            }

            if (listQuery.getMemberCodes() != null && !listQuery.getMemberCodes().isEmpty()) {
                whereQuery += " AND LOWER(im.code) IN :memberCodes";
            }

            String groupByQuery = getGroupByQuery(listQuery, institution);
            String orderBy = (listQuery.getWithReceipt()!=null && listQuery.getWithReceipt())
                    ? " ORDER BY re.entryDate DESC"
                    : " ORDER BY im.code DESC";

            // Typed Queries
            String selectQueryString = selectQuery + commonPart + whereQuery + groupByQuery + orderBy,
                    countQueryString = countQuery + commonPart + whereQuery,
                    sumQueryString = sumQuery + commonPart + whereQuery + groupByQuery;
            TypedQuery<DonationReport> donationReportTypedQuery = entityManager.createQuery(
                    selectQueryString,
                    DonationReport.class);
            TypedQuery<Long> donationCountQuery = entityManager.createQuery(
                    countQueryString,
                    Long.class);
            TypedQuery<Long> sumCountQuery = sumQuery!=null
                    ? entityManager.createQuery(
                            sumQueryString,
                    Long.class)
                    : null;

            donationReportTypedQuery.setParameter("institutionId", listQuery.getInstitutionId());
            donationCountQuery.setParameter("institutionId", listQuery.getInstitutionId());
            if(sumCountQuery!=null) {
                sumCountQuery.setParameter("institutionId", listQuery.getInstitutionId());
            }

            if(listQuery.getActive()!=null){
                boolean isActive = listQuery.getActive();
                donationReportTypedQuery.setParameter("memberActive", isActive);
                donationCountQuery.setParameter("memberActive", isActive);
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("memberActive", isActive);
                }
            }

            if (!StringUtils.isEmpty(listQuery.getQuery())) {
                donationReportTypedQuery.setParameter("query", "%"+listQuery.getQuery().trim()+"%");
                donationCountQuery.setParameter("query", "%"+listQuery.getQuery().trim()+"%");
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("query", "%"+listQuery.getQuery().trim()+"%");
                }
            }

            if (listQuery.getSubscription() != null && !listQuery.getSubscription().isEmpty()){
                donationReportTypedQuery.setParameter("subscription", listQuery.getSubscription());
                donationCountQuery.setParameter("subscription", listQuery.getSubscription());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("subscription", listQuery.getSubscription());
                }
            }

            if(listQuery.getWithReceipt()!=null) {
                if(listQuery.getWithReceipt()) {
                    if (listQuery.getPaymentMode() != null && !listQuery.getPaymentMode().isEmpty()) {
                        donationReportTypedQuery.setParameter("paymentModes", listQuery.getPaymentMode());
                        donationCountQuery.setParameter("paymentModes", listQuery.getPaymentMode());
                        if (sumCountQuery != null) {
                            sumCountQuery.setParameter("paymentModes", listQuery.getPaymentMode());
                        }
                    }

                    if (listQuery.getCreditAccountId() != null) {
                        donationReportTypedQuery.setParameter("creditAccount", listQuery.getCreditAccountId());
                        donationCountQuery.setParameter("creditAccount", listQuery.getCreditAccountId());
                        if (sumCountQuery != null) {
                            sumCountQuery.setParameter("creditAccount", listQuery.getCreditAccountId());
                        }
                    }

                    if (listQuery.getMinAmount() != null) {
                        donationReportTypedQuery.setParameter("minAmount", listQuery.getMinAmount());
                        donationCountQuery.setParameter("minAmount", listQuery.getMinAmount());
                        if (sumCountQuery != null) {
                            sumCountQuery.setParameter("minAmount", listQuery.getMinAmount());
                        }
                    }
                    if (listQuery.getMaxAmount() != null) {
                        donationReportTypedQuery.setParameter("maxAmount", listQuery.getMaxAmount());
                        donationCountQuery.setParameter("maxAmount", listQuery.getMaxAmount());
                        if (sumCountQuery != null) {
                            sumCountQuery.setParameter("maxAmount", listQuery.getMaxAmount());
                        }
                    }

                    if (listQuery.getCategories() != null && !listQuery.getCategories().isEmpty()) {
                        donationReportTypedQuery.setParameter("categoriesList", listQuery.getCategories());
                        donationCountQuery.setParameter("categoriesList", listQuery.getCategories());
                        if (sumCountQuery != null) {
                            sumCountQuery.setParameter("categoriesList", listQuery.getCategories());
                        }
                    }
                }
            } else {
                if(listQuery.getCategories()!=null && !listQuery.getCategories().isEmpty()) {
                    donationReportTypedQuery.setParameter("categoriesList", listQuery.getCategories());
                    donationCountQuery.setParameter("categoriesList", listQuery.getCategories());
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("categoriesList", listQuery.getCategories());
                    }
                }
            }

            Calendar calendar = Calendar.getInstance();
            Date dateTwo,
                    today = DateUtils.getNowDateTime();
            switch (listQuery.getDuration()) {
                case TODAY:
                case SINGLE_DATE:
                    if (listQuery.getDuration().equals(SINGLE_DATE) && listQuery.getFrom() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date not specified.");
                    }
                    Date chosenDate = (listQuery.getDuration().equals(DurationGap.TODAY))
                            ? today
                            : DateUtils.convertToTimezone(listQuery.getFrom(), listQuery.getClientTimezone());
                    donationReportTypedQuery.setParameter("fromDate", chosenDate);
                    donationCountQuery.setParameter("fromDate", chosenDate);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("fromDate", chosenDate);
                    }
                    break;

                case LAST_SEVEN_DAYS:
                    calendar.add(Calendar.DAY_OF_WEEK, -7);
                    dateTwo = calendar.getTime();
                    donationReportTypedQuery.setParameter("fromDate", dateTwo);
                    donationCountQuery.setParameter("fromDate", dateTwo);
                    donationCountQuery.setParameter("fromDate", dateTwo);
                    donationReportTypedQuery.setParameter("toDate", today);
                    donationCountQuery.setParameter("toDate", today);
                    donationCountQuery.setParameter("toDate", today);
                    break;

                case LAST_MONTH:
                case LAST_THREE_MONTH:
                case LAST_SIX_MONTH:
                    int months = -1;
                    if (listQuery.getDuration().equals(DurationGap.LAST_THREE_MONTH)) months = -3;
                    if (listQuery.getDuration().equals(LAST_SIX_MONTH)) months = -6;
                    calendar.add(Calendar.MONTH, months);
                    dateTwo = calendar.getTime();
                    donationReportTypedQuery.setParameter("fromDate", dateTwo);
                    donationCountQuery.setParameter("fromDate", dateTwo);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("fromDate", dateTwo);
                    }
                    donationReportTypedQuery.setParameter("toDate", today);
                    donationCountQuery.setParameter("toDate", today);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("toDate", today);
                    }
                    break;

                case YEAR:
                    dateTwo = DateUtils.getFirstDateOfYear(null);
                    donationReportTypedQuery.setParameter("fromDate", dateTwo);
                    donationCountQuery.setParameter("fromDate", dateTwo);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("fromDate", dateTwo);
                    }
                    donationReportTypedQuery.setParameter("toDate", today);
                    donationCountQuery.setParameter("toDate", today);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("toDate", today);
                    }
                    break;

                case CUSTOM_DATE_GAP:
                    if (listQuery.getFrom() == null || listQuery.getTo() == null) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Date(s) not specified.");
                    }
                    if (listQuery.getFrom().after(listQuery.getTo())) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Ending date must be a date after the Starting date.");
                    }
                    Date fromDate = listQuery.getFrom(),
                            toDate = listQuery.getTo();
                    donationReportTypedQuery.setParameter("fromDate", fromDate);
                    donationCountQuery.setParameter("fromDate", fromDate);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("fromDate", fromDate);
                    }
                    donationReportTypedQuery.setParameter("toDate", toDate);
                    donationCountQuery.setParameter("toDate", toDate);
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("toDate", toDate);
                    }
                    break;
            }

            if (listQuery.getPincode() != null) {
                donationReportTypedQuery.setParameter("pincode", listQuery.getPincode());
                donationCountQuery.setParameter("pincode", listQuery.getPincode());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("pincode", listQuery.getPincode());
                }
            }
            if (listQuery.getPhone() != null) {
                donationReportTypedQuery.setParameter("phone", listQuery.getPhone());
                donationCountQuery.setParameter("phone", listQuery.getPhone());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("phone", listQuery.getPhone());
                }
            }
            if (listQuery.getAlternatePhone() != null) {
                donationReportTypedQuery.setParameter("alternatePhone", listQuery.getAlternatePhone());
                donationCountQuery.setParameter("alternatePhone", listQuery.getAlternatePhone());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("alternatePhone", listQuery.getAlternatePhone());
                }
            }
            if (listQuery.getCountry()!=null && !listQuery.getCountry().isEmpty()) {
                donationReportTypedQuery.setParameter("country", listQuery.getCountry());
                donationCountQuery.setParameter("country", listQuery.getCountry());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("country", listQuery.getCountry());
                }
            }
            if (listQuery.getStates() != null && !listQuery.getStates().isEmpty()) {
                donationReportTypedQuery.setParameter("addressStates", listQuery.getStates());
                donationCountQuery.setParameter("addressStates", listQuery.getStates());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("addressStates", listQuery.getStates());
                }
            } else {
                if (!StringUtils.isEmpty(listQuery.getState())) {
                    donationReportTypedQuery.setParameter("addressStates", listQuery.getState());
                    donationCountQuery.setParameter("addressStates", listQuery.getState());
                    if(sumCountQuery!=null) {
                        sumCountQuery.setParameter("addressStates", listQuery.getState());
                    }
                }
            }
            if(!StringUtils.isEmpty(listQuery.getDistrict())){
                donationReportTypedQuery.setParameter("district", listQuery.getDistrict());
                donationCountQuery.setParameter("district", listQuery.getDistrict());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("district", listQuery.getDistrict());
                }
            }
            if (listQuery.getMemberCodes() != null && !listQuery.getMemberCodes().isEmpty()) {
                List<String> lowerCaseMemberCodes = listQuery.getMemberCodes()
                                .parallelStream()
                                        .map(String::toLowerCase).collect(Collectors.toList());
                donationReportTypedQuery.setParameter("memberCodes", lowerCaseMemberCodes);
                donationCountQuery.setParameter("memberCodes", lowerCaseMemberCodes);
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("memberCodes", lowerCaseMemberCodes);
                }
            }

            if(!institution.getInstitutionType().equals(InstitutionType.CHURCH)
                    && listQuery.getWithReceipt()!=null && listQuery.getWithReceipt()) {
                if (listQuery.getCategories() != null && !listQuery.getCategories().isEmpty()) {
                    donationReportTypedQuery.setParameter("categoriesList", listQuery.getCategories());
                    donationCountQuery.setParameter("categoriesList", listQuery.getCategories());
                    if (sumCountQuery != null) {
                        sumCountQuery.setParameter("categoriesList", listQuery.getCategories());
                    }
                }
            }

            if (listQuery.getLanguages() != null && !listQuery.getLanguages().isEmpty()) {
                donationReportTypedQuery.setParameter("languagesList", listQuery.getLanguages());
                donationCountQuery.setParameter("languagesList", listQuery.getLanguages());
                if(sumCountQuery!=null) {
                    sumCountQuery.setParameter("languagesList", listQuery.getLanguages());
                }
            }


            // Set pagination parameters
            int page = listQuery.getPage() - 1;
            if (page < 0) page = 0;
            donationReportTypedQuery.setFirstResult(page * listQuery.getSize());
            donationReportTypedQuery.setMaxResults(listQuery.getSize());
            LinkedList<DonationReport> results = new LinkedList<>(donationReportTypedQuery.getResultList());

            Long resultsCount = donationCountQuery.getSingleResult();
            resultsCount = (resultsCount==null) ? 0 : resultsCount;
            AtomicLong totalResult = new AtomicLong(resultsCount);

            AtomicReference<Long> total = new AtomicReference<>(0L);

            Map<Long, String> availableCategories;
            if(listQuery.getCategories()!=null) {
                if (!listQuery.getCategories().isEmpty()) {
                    availableCategories = (institution.getInstitutionType().equals(InstitutionType.GENERAL))
                            ? appRepository.getCategoryRepository()
                            .findAllByIdIn(listQuery.getCategories())
                            .parallelStream()
                            .collect(Collectors.toMap(InstitutionCategory::getId, InstitutionCategory::getName))

                            : appRepository.getChurchContributionRepository()
                            .findAllByIdIn(listQuery.getCategories())
                            .parallelStream()
                            .collect(Collectors.toMap(ChurchContribution::getId, ChurchContribution::getName));
                } else {
                    availableCategories = null;
                }
            } else {
                availableCategories = null;
            }

            Map<String, DonationReport> resultsMap = new HashMap<>();

            results.forEach(singleDonation->{
                        Set<String> categoriesNames = availableCategories==null
                                ? new HashSet<>()
                                : new HashSet<>(availableCategories.values());
                        if (availableCategories!=null && !categoriesNames.contains(singleDonation.getCategory())){
                            totalResult.getAndDecrement();
                        } else {
                            // Aha in this case yacaguye abadafise recus
                            if(listQuery.getWithReceipt()!=null && listQuery.getWithReceipt()) {
                                total.updateAndGet(v -> v + singleDonation.getAmount());
                            }
                        }

                        if(listQuery.getWithReceipt()!=null && listQuery.getWithReceipt()){
                            // merge the objects;
                            if(!resultsMap.containsKey(singleDonation.getReceiptNo())){
                                singleDonation.addCategory(singleDonation.getCategory());
                                resultsMap.put(singleDonation.getReceiptNo(), singleDonation);
                            } else {
                                DonationReport theDonation = resultsMap.get(singleDonation.getReceiptNo());
                                theDonation.addCategory(singleDonation.getCategory());
                                theDonation.addAmount(singleDonation.getAmount());
                            }
                        }
                    });
            if(!resultsMap.isEmpty()) results = new LinkedList<>(resultsMap.values());
            Page<DonationReport> reports = new PageImpl<>(results,
                    PageRequest.of(page, listQuery.getSize()), totalResult.get());

            receiptor = new Receiptor();
            receiptor.setReports(reports);
            receiptor.setTotal(total.get());

        } catch(Exception exception){
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Failed to execute your request. Please try again later.");
        } finally {
            entityManager.close();
        }
        return receiptor;
    }


    private static String getGroupByQuery(AddressRequestModel listQuery, Institution institution) {
        String groupByQuery = " GROUP BY im.id, im.phoneCode, im.phone, im.code, im.firstName, im.lastName";
        if(listQuery.getWithReceipt()!=null && listQuery.getWithReceipt()) {
            groupByQuery = " GROUP BY re.id, re.receiptNo, re.entryDate, im.code, re.paymentMode, im.firstName, im.lastName, isd.amount";
            groupByQuery += ", " + (institution.getInstitutionType().equals(InstitutionType.GENERAL) ? "ic.name" : "cc.name");
        } else {
            groupByQuery += ", im.gender";
        }
        return groupByQuery;
    }


    public Long getTotal(){
        AtomicLong total = new AtomicLong(0);
        if(this.getDonations()!=null) {
            this.getDonations().parallelStream().forEach(donation -> {
                total.addAndGet(donation.getAmount());
            });
        }
        return total.get();
    }



    @Transient
    @JsonIgnore
    private String getDefaultSMSMessage(){
        return "Dear " +
                this.getMember().getFirstName() +
                ": Receipt " +
                this.getReceiptNo() +
                " - We've received a generous contribution of " +
                this.getTotal() +
                ". Thanks for you support -Yesu Vazkirar. Dnote.ai";
    }

    @Transient
    @JsonIgnore
    public String getSMSMessage(@NonNull AppRepository appRepository,
                                @NonNull Institution institution){
        Template template = appRepository.getTemplateRepository()
                .findUniqueTemplate(this.getInstitutionId() , TemplateStyle.SMS_RECEIPT)
                .orElse(null);
        if(template==null) return this.getDefaultSMSMessage();
        this.getMember().setInstitution(institution);
        return Messenger.getMessage(template.getSmsText(), this.getMember());
    }

    /*
    @Transient
    @JsonIgnore
    public String getWhatsappMessage(@NonNull AppRepository appRepository,
                                @NonNull InstitutionReceipt receipt,
                                @NonNull Institution institution){
        String message = this.getDefaultWhatsappMessage();
        Template template = appRepository.getTemplateRepository()
                .findUniqueTemplate(this.getInstitutionId() , TemplateStyle.RECEIPT)
                .orElse(null);
        if(template==null) return message;
        message = Messenger.getMessage(template.getSmsText(), this.getMember(), institution);
        message = message.replace("{{amount}}", receipt.getTotal()+"");
        message = message.replace("{{receiptno}}", receipt.getTotal()+"");

        String dateString = DateUtils.getDateString()
        message = message.replace("{{date}}", dateString);
        return message;
    }
     */



    @Transient
    private List<String> paidSubscriptions;
    public void computePaidSubscriptions(@NonNull AppRepository appRepository,
                                         @NonNull Institution institution){
        if(this.getDonations()==null){
            this.donations = appRepository.getInstitutionDonationRepository()
                    .findAllByReceiptNoAndInstitutionId(this.getReceiptNo(), this.getInstitutionId());
        }
        List<Long> subscriptionsIds = this.getDonations().parallelStream()
                .map(singleDonation->{
                    return institution.getInstitutionType().equals(InstitutionType.GENERAL)
                            ? singleDonation.getCategoryId()
                            : singleDonation.getChurchContributionId();
                }).collect(Collectors.toList());

        this.paidSubscriptions = institution.getInstitutionType().equals(InstitutionType.GENERAL)
                ? appRepository.getCategoryRepository()
                    .findAllByIdIn(subscriptionsIds)
                        .parallelStream()
                            .map(InstitutionCategory::getName)
                                .collect(Collectors.toList())
                : appRepository.getChurchContributionRepository()
                    .findAllByIdIn(subscriptionsIds)
                        .parallelStream()
                            .map(ChurchContribution::getName)
                                .collect(Collectors.toList());
    }
    @Transient
    @JsonIgnore
    public String getCommunicationMessage(String specificMessage){
        String paidSubscriptionsString = String.join(", ", this.getPaidSubscriptions());
        specificMessage = specificMessage.replace("{{receiptno}}", this.getReceiptNo());
        specificMessage = specificMessage.replace("[[receiptno]]", this.getReceiptNo());

        specificMessage = specificMessage.replace("{{receiptdate}}", DateUtils.getDateString(this.getEntryDate()));
        specificMessage = specificMessage.replace("[[receiptdate]]", DateUtils.getDateString(this.getEntryDate()));

        specificMessage = specificMessage.replace("{{receiptpayment}}", this.getPaymentMode().getDisplayName());
        specificMessage = specificMessage.replace("[[receiptpayment]]", this.getPaymentMode().getDisplayName());

        if(this.getPaidSubscriptions()!=null && !this.getPaidSubscriptions().isEmpty()) {
            specificMessage = specificMessage.replace("{{subscriptions}}", paidSubscriptionsString.toUpperCase());
            specificMessage = specificMessage.replace("[[subscriptions]]", paidSubscriptionsString.toUpperCase());
        }
        Long total = this.getTotal();
        if(total!=null){
            specificMessage = specificMessage.replace("{{amount}}", "Rs."+total);
            specificMessage = specificMessage.replace("[[amount]]", "Rs."+total);
        }
        return specificMessage;
    }

    public InstitutionReceipt save(@NonNull AppRepository appRepository,
                                   @NonNull Institution institution){
        Calendar calendar = Calendar.getInstance();
        Date monthStart = DateUtils.getFirstDateOfMonth(this.getEntryDate()),
                monthEnd = DateUtils.getLastDayOfMonth(this.getEntryDate());
        int tries = 0;
        InstitutionReceipt newReceipt = null;
        for(; tries<200; tries++){
            try{
                newReceipt = appRepository.getInstitutionReceiptsRepository()
                        .save(this);
                break;
            } catch(Exception exception){
                InstitutionReceipt lastReceipt = appRepository.getInstitutionReceiptsRepository()
                        .findFirstByInstitutionIdOrderByIdDesc(institution.getId());
                Long lastDonationNumber = institution.retrieveDonationNumber(lastReceipt.getReceiptNo());
                if(lastDonationNumber==null) {
                    lastDonationNumber = appRepository.getInstitutionReceiptsRepository()
                            .countReceiptsFromDate(monthStart, monthEnd, institution.getId());
                }
                String newNumber = institution.generateReceiptNo(appRepository, lastDonationNumber+tries);
                this.setReceiptNo(newNumber);
            }
        }
        if(newReceipt==null){
            appRepository.getInstitutionDonationRepository()
                            .deleteAllByReceiptNo(this.getReceiptNo());
            CompletableFuture.runAsync(()->institution.autoCorrectData(appRepository));
        }
        appRepository.getInstitutionRepository().save(institution);
        return newReceipt;
    }
}
