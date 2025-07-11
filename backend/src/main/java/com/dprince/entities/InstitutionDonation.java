package com.dprince.entities;

import com.dprince.apis.institution.models.donations.DonationContribution;
import com.dprince.apis.institution.validators.ChurchContributionValidator;
import com.dprince.apis.institution.validators.GeneralDonationValidation;
import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.PaymentMode;
import com.dprince.entities.parts.TopDonation;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@FieldNameConstants
@Data
@Entity
@Table(name = "institution_donations",
        indexes={
            @Index(name = "idx_member_id", columnList = "memberId"),
            @Index(name = "idx_amount", columnList = "amount"),
        })
public class InstitutionDonation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 30)
    private String receiptNo;

    @JsonIgnore
    @Transient
    private InstitutionReceipt receipt;

    @JsonIgnore
    @Column(nullable = false)
    private Long institutionId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long creditAccount;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Institution institution;

    @NotNull(message = "Please specify the Donator/Contributor.",
            groups={
                GeneralDonationValidation.class,
                ChurchContributionValidator.class
    })
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private Long memberId;

        @Transient
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private InstitutionMember member;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long categoryId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private InstitutionCategory category;


    @NotNull(message = "Please select the type of contribution.",
            groups={
                    ChurchContributionValidator.class
        })
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long churchContributionId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ChurchContribution contribution;

    public void populate(AppRepository appRepository,
                         Institution institution){
        if(institution.getInstitutionType().equals(InstitutionType.GENERAL)){
            if(this.getCategoryId()!=null) {
                appRepository.getCategoryRepository()
                        .findById(this.getCategoryId())
                        .ifPresent(this::setCategory);
            }
        } else {
            if (this.getChurchContributionId() != null) {
                appRepository
                        .getChurchContributionRepository()
                        .findById(this.getChurchContributionId())
                        .ifPresent(this::setContribution);
            }
        }
    }

    @Transient
    @NotNull(message = "Please specify the Payment Mode.",
            groups = {
                    GeneralDonationValidation.class,
                    ChurchContributionValidator.class
            })
    private PaymentMode paymentMode;


    @NotNull(message = "Please provide the donated/contributed amount",
            groups={
                    GeneralDonationValidation.class,
                    ChurchContributionValidator.class
            })
    @Min(value = 5, message = "At least Five Rupee(s) should be donated.",
            groups={
                    GeneralDonationValidation.class,
                    ChurchContributionValidator.class
            })
    @Column(nullable = false)
    private Long amount;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String subscription;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @PastOrPresent(message = "Entry date must be a date in present orin the past.")
    private Date entryDate = DateUtils.getNowDateTime();

    @Transient
    public boolean fillSubscription(AppRepository appRepository){
        boolean filled = false;
        if(this.getMember()==null) {
            appRepository.getInstitutionMemberRepository()
                    .findById(this.getMemberId())
                    .ifPresent(this::setMember);
        }


        if(this.getInstitution()==null) {
            appRepository.getInstitutionRepository()
                    .findById(this.getInstitutionId())
                    .ifPresent(this::setInstitution);
        }

        if(this.getInstitution()==null || this.getMember()==null) return false;

        if(this.getChurchContributionId()!=null){
            ChurchContribution contribution = appRepository.getChurchContributionRepository()
                    .findById(this.getChurchContributionId()).orElse(null);
            if(contribution==null) return false;
            this.setSubscription(contribution.getName());
            filled = true;
        } else {
            this.setSubscription(this.getMember().getSubscription().name());
            filled = true;
        }
        return filled;
    }


    @Transient
    @JsonIgnore
    public InstitutionDonation save(AppRepository appRepository){
        InstitutionDonation donation = appRepository.getInstitutionDonationRepository()
                .save(this);
        if(this.getReceipt()==null) {
            // Create a new receipt.
            InstitutionReceipt receipt = new InstitutionReceipt();
            receipt.setInstitutionId(member.getInstitutionId());
            receipt.setReceiptNo(this.getReceiptNo());
            receipt.setMemberId(member.getId());
            receipt.setPaymentMode(this.getPaymentMode());
            receipt.setEntryDate(this.getEntryDate());
            receipt.setCreditAccountId(this.getCreditAccount());
            InstitutionReceipt savedReceipt = receipt.save(appRepository, this.getInstitution());
            if (savedReceipt != null){
                if(this.getInstitution()!=null){
                    InstitutionMember member = this.getMember();
                    if(this.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)){
                        member.addCategory(this.getCategoryId());
                    } else {
                        member.addContribution(this.getChurchContributionId());
                    }
                    savedReceipt.addDonation(this);
                    member.setInstitution(this.getInstitution());
                    DonationContribution donationContribution = new DonationContribution();
                    donationContribution.setMemberId(member.getId());
                    member.subscribe(appRepository, member, savedReceipt, null);
                    appRepository.getInstitutionMemberRepository().save(member);
                }
            } else {
                receipt.delete(appRepository);
            }
        }
        return donation;
    }


    public static Long getOrganizationSumDonations(EntityManager entityManager,
                                            Long institutionId,
                                            Long categoryId,
                                            Date startDate,
                                            Date endDate){
        long result;
        try {
            String sql = "SELECT SUM(isd.amount) FROM InstitutionDonation isd" +
                    " INNER JOIN InstitutionReceipt inr ON isd.receiptNo=inr.receiptNo" +
                    " WHERE isd.institutionId=:institutionId" +
                    " AND isd.categoryId=:categoryId" +
                    " AND (CONVERT(DATE, isd.entryDate)>=CONVERT(DATE, :startDate) AND CONVERT(DATE, isd.entryDate)<CONVERT(DATE, :endDate))";
            TypedQuery<Long> query = entityManager.createQuery(sql, Long.class);
            query.setParameter("institutionId", institutionId);
            query.setParameter("categoryId", categoryId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            Long foundResult = query.getSingleResult();
            result = (foundResult == null) ? 0 : foundResult;
        } finally {
            entityManager.close();
        }
        return result;
    }


    public static List<TopDonation> getTopDonations(EntityManager entityManager,
                                                    Long institutionId,
                                                    Date startDate,
                                                    Date endDate){
        List<TopDonation> foundResults;
        try {
            String sql = "SELECT new com.dprince.entities.parts.TopDonation(" +
                    " isd.memberId as memberId," +
                    " isd.amount as amount," +
                    " isd.categoryId as categoryId," +
                    " isd.churchContributionId as churchContributionId" +
                    ") FROM InstitutionDonation isd" +
                    " INNER JOIN InstitutionReceipt inr ON isd.receiptNo=inr.receiptNo" +
                    " WHERE isd.institutionId=:institutionId" +
                    " AND isd.entryDate BETWEEN :startDate AND :endDate" +
                    " ORDER BY isd.amount DESC";
            TypedQuery<TopDonation> query = entityManager.createQuery(sql, TopDonation.class);
            query.setParameter("institutionId", institutionId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            query.setFirstResult(0);
            query.setMaxResults(5);
            foundResults = query.getResultList();
        } finally {
            entityManager.close();
        }
        return foundResults;
    }

    @JsonIgnore
    @Transient
    public boolean isAlreadyPresent(@NonNull AppRepository appRepository){
        Long count = appRepository.getInstitutionDonationRepository().countSimilar(this.getInstitutionId(),
                this.getMemberId(),
                this.getReceiptNo(),
                this.getAmount(),
                this.getChurchContributionId(),
                this.getCategoryId(),
                this.getEntryDate());
        if(count==null) count = 0L;
        return count>0;
    }


    /**
     * This overrides the equals so that we can overcome the problem with multiple donations entry
     * @param o: Parameter of type InstitutionDonation
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        List<Boolean> equalityList = new ArrayList<>();
        InstitutionDonation theObject = (InstitutionDonation) o;
        equalityList.add(Objects.equals(this.getAmount(), theObject.getAmount()));
        equalityList.add(Objects.equals(this.getChurchContributionId(), theObject.getChurchContributionId()));
        equalityList.add((this.getEntryDate().compareTo(theObject.getEntryDate())==0));
        equalityList.add(Objects.equals(this.getInstitutionId(), theObject.getInstitutionId()));
        equalityList.add(Objects.equals(this.getMemberId(), theObject.getMemberId()));
        equalityList.add(Objects.equals(this.getReceiptNo(), theObject.getReceiptNo()));
        equalityList.add(Objects.equals(this.getCategoryId(), theObject.getCategoryId()));

        return !equalityList.contains(false);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.getAmount(),
                this.getChurchContributionId(),
                this.getEntryDate(),
                this.getInstitutionId(),
                this.getMemberId(),
                this.getReceiptNo(),
                this.getCategoryId());
    }

}