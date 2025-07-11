package com.dprince.apis.dashboard.vos;

import com.dprince.apis.dashboard.utils.enums.DashboardDuration;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.LinkedHashMap; // Keep LinkedHashMap if you use it elsewhere
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DashboardStats {

    private Long institutions;
    private Long churches;
    private Long churchBranches;
    private Long organizations;
    private Long overallFamilies;
    private Long families;

    private Long churchFamilies;
    private Long organizationFamilies;

    private Long overAllMales;
    private Long males;

    private Long overAllFemales;
    private Long females;

    private Long active;
    private Long inactive;

    private Long membersInIndia;
    private Long membersAbroad;

    private Long weekRegistrations;
    private Long admins;


    private Long churchAdmins;
    private Long churchAssistantAdmins;

    private Long organizationAdmins;
    private Long organizationAssistantAdmins;

    private Long blockedInstitution;
    private Long blockedChurches;
    private Long blockedOrganizations;
    private Long usedSms;
    private Long maxSms;
    private Long usedEmails;
    private Long maxEmails;
    private Long usedWhatsapp;
    private Long maxWhatsapp;


    private Long churchUsedSms;
    private Long churchUsedEmails;
    private Long churchUsedWhatsapp;

    private Long organizationUsedSms;
    private Long organizationUsedEmails;
    private Long organizationUsedWhatsapp;

    private Long smsTrans;
    private Long smsPromo;


    private String expirationMessage;


    private Map<String, Long> partnerCategoryWiseStats;
    private Map<DashboardDuration, Map<String, Long>> categoryWiseDonations;


    // >>>>> இந்த இரண்டு வரிகளை மட்டும் மாற்றவும் <<<<<
    private Map<DashboardDuration, List<Map<String, Object>>> churchRegistrations;
    private Map<DashboardDuration, List<Map<String, Object>>> orgRegistrations;


    private LinkedHashMap<String, Long> organizationsPlans;
    private LinkedHashMap<String, Long> churchesPlans;


    private List<InstitutionTopper> churchToppers;
    private List<InstitutionTopper> organizationToppers;


    // genders;
    private Long churchMales;
    private Long churchFemales;
    private Long organizationMales;
    private Long organizationFemales;


    private Map<DashboardDuration, Map<String, Long>> institutionWiseDonation;
    private Map<DashboardDuration, List<TopMember>> institutionWiseTopMembers;

    private Map<DashboardDuration, Map<String, Long>> membersByCategory;

    private Long activeMembers;
    private Long inactiveMembers;
    private Long baptizedMale;
    private Long baptizedFemale;
    private Long communionMale;
    private Long communionFemale;


    private Long babiesMale;
    private Long babiesFemale;
    private Long childrenMale;
    private Long childrenFemale;
    private Long teenageMale;
    private Long teenageFemale;
    private Long youthMale;
    private Long youthFemale;
    private Long adultMale;
    private Long adultFemale;


    private Long birthdays;
    private Long marriages;
}