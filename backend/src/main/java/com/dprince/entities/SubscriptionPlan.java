package com.dprince.entities;

import com.dprince.apis.dashboard.vos.DashboardStats;
import com.dprince.configuration.database.converters.ListOfAppFeatureConverter;
import com.dprince.entities.enums.AppFeature;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@FieldNameConstants
@Entity
@Table(name = "subscription_plans",
        indexes = {
            @Index(name = "sub_institution_type", columnList = "institutionType")
        })
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private InstitutionType institutionType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long smses = 0L;

    @Column(nullable = false)
    private Long emails = 0L;

    @Column(nullable = false)
    private Long whatsapp = 0L;

    @Column(nullable = false)
    private Long admins = 0L;

    @Column(nullable = false)
    private Long families = 0L;

    @Column(nullable = false)
    private Long members = 0L;

    private Long churchBranches= 0L;

    @Column(nullable = false)
    private Long price = 0L;

    @Column(length = Integer.MAX_VALUE)
    @Convert(converter = ListOfAppFeatureConverter.class)
    private List<AppFeature> features;

    public void addTopUp(TopUp topUp){
        if(this.smses == null ) this.smses = 0L;
        if(topUp.getSms()!=null && topUp.getSms()>0) this.smses += topUp.getSms();

        if(this.emails == null ) this.emails = 0L;
        if(topUp.getEmail()!=null && topUp.getEmail()>0) this.emails += topUp.getEmail();

        if(this.whatsapp == null ) this.whatsapp = 0L;
        if(topUp.getWhatsapp()!=null && topUp.getWhatsapp()>0) this.whatsapp += topUp.getWhatsapp();

        if(this.admins == null ) this.admins = 0L;
        if(topUp.getAdditionalUser()!=null && topUp.getAdditionalUser()>0) this.admins += topUp.getAdditionalUser();

        if(this.families == null ) this.families = 0L;
        if(topUp.getFamilies()!=null && topUp.getFamilies()>0) this.families += topUp.getFamilies();

        if(this.members == null ) this.members = 0L;
        if(topUp.getMembers()!=null && topUp.getMembers()>0) this.members += topUp.getMembers();

        if(this.churchBranches == null ) this.churchBranches = 0L;
        if(topUp.getChurchBranches()!=null && topUp.getChurchBranches()>0) this.churchBranches += topUp.getChurchBranches();
    }


    @Transient
    @JsonIgnore
    public static DashboardStats getStats(@NonNull EntityManager entityManager,
                                          @NonNull AppRepository appRepository,
                                          @NonNull DashboardStats dashboardStats){
        List<SubscriptionPlan> allPlans = appRepository.getSubscriptionPlanRepository()
                .findAll();

        allPlans.forEach(singlePlan->{
            Long total = SubscriptionPlan.getStatsForPlan(entityManager, singlePlan);
            if(singlePlan.getInstitutionType().equals(InstitutionType.CHURCH)){
                if(dashboardStats.getChurchesPlans()==null)
                    dashboardStats.setChurchesPlans(new LinkedHashMap<>());
                dashboardStats.getChurchesPlans().put(singlePlan.getName(), total);
            } else {
                if(dashboardStats.getOrganizationsPlans()==null)
                    dashboardStats.setOrganizationsPlans(new LinkedHashMap<>());
                dashboardStats.getOrganizationsPlans().put(singlePlan.getName(), total);
            }
        });
        return dashboardStats;
    }

    @Transient
    @JsonIgnore
    private static Long getStatsForPlan(@NonNull EntityManager entityManager,
                                SubscriptionPlan subscriptionPlan){
        String countQuery = "SELECT COUNT(ins) FROM Institution ins" +
                " INNER JOIN SubscriptionPlan s ON ins.subscriptionPlan=s.id" +
                " AND ins.subscriptionPlan=:planId";

        TypedQuery<Long> typedQuery = entityManager.createQuery(countQuery, Long.class);

        typedQuery.setParameter("planId", subscriptionPlan.getId());
        return typedQuery.getSingleResult();
    }
}
