package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.Subscriptionable;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.Subscription;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.Date;

@FieldNameConstants
@Data
@Entity
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "index_institution_id", columnList = "institutionId"),
                @Index(name = "index_member_id", columnList = "memberId"),
                @Index(name = "index_category_id", columnList = "categoryId"),
                @Index(name = "index_contribution_id", columnList = "contributionId")
        })
public class PeopleSubscription implements Subscriptionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private Long memberId;

    private Long categoryId;

    private Long contributionId;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date start = DateUtils.getNowDateTime();

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription subscription = Subscription.ANNUAL;

    public static PeopleSubscription makeSubscription(InstitutionMember member,
                                                      Subscription subscription,
                                                      InstitutionDonation donation){
        Institution institution = member.getInstitution();
        PeopleSubscription peopleSubscription = new PeopleSubscription();
        peopleSubscription.setStart(donation.getEntryDate());
        peopleSubscription.setMemberId(member.getId());
        peopleSubscription.setInstitutionId(member.getInstitutionId());
        peopleSubscription.setSubscription(subscription);
        peopleSubscription.computeDeadline(subscription);
        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            peopleSubscription.setContributionId(donation.getChurchContributionId());
            if(member.getContributionsIds()==null || !member.getContributionsIds().contains(donation.getChurchContributionId())) {
                member.addContribution(donation.getChurchContributionId());
            }
        } else {
            peopleSubscription.setCategoryId(donation.getCategoryId());
            if(member.getCategoriesIds()==null || !member.getCategoriesIds().contains(donation.getCategoryId())) {
                member.addCategory(donation.getCategoryId());
            }
        }
        return peopleSubscription;
    }
}
