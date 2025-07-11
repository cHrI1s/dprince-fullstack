package com.dprince.apis.dashboard;

import com.dprince.apis.dashboard.utils.DashboardUtils;
import com.dprince.apis.dashboard.utils.enums.DashboardDuration;
import com.dprince.apis.dashboard.vos.DashboardStats;
import com.dprince.apis.dashboard.vos.TopMember;
import com.dprince.apis.dashboard.vos.parts.CategorizedStats;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.vos.DateGap;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.sms.SmsService;
import com.dprince.configuration.sms.vos.SmsBalance;
import com.dprince.entities.*;
import com.dprince.entities.enums.BaptismStatus;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.parts.UniqueDonation;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.OverallInstitutionStats;
import com.dprince.startup.GeneralValues;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/dashboard")
public class DashboardController {
    private final AppRepository appRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final SmsService smsService;
    private final ExecutorService executorService;

    private EntityManager getEntityManager() {
        return this.entityManagerFactory.createEntityManager();
    }

    private final List<DashboardDuration> dashboardDurations = Arrays.asList(DashboardDuration.values());

    @Authenticated
    @GetMapping("/home")
    public DashboardStats getHomeStats(@NonNull HttpSession session) throws Exception {
        User loggedInUser = User.getUserFromSession(session);
        DashboardStats dashboardStats = new DashboardStats();

        Date today = DateUtils.getNowDateTime();
        long institutions,
                families,
                females,
                males;
        AtomicLong admins = new AtomicLong(0);
        Institution institution;

        if (User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            institution = null;
            institutions = this.appRepository.getInstitutionRepository()
                    .count();
            dashboardStats.setInstitutions(institutions);


            families = this.appRepository.getInstitutionFamilyRepository().countFamilies();
            dashboardStats.setOverallFamilies(families);


            // To review
            females = this.appRepository.getInstitutionMemberRepository()
                    .countAllByGender(Gender.FEMALE);
            males = this.appRepository.getInstitutionMemberRepository()
                    .countAllByGender(Gender.MALE);
            dashboardStats.setOverAllFemales(females);
            dashboardStats.setOverAllMales(males);


            // Institution;
            OverallInstitutionStats overallInstitutionStats = Institution.getStats(this.getEntityManager());
            dashboardStats.setUsedSms(overallInstitutionStats.getSms());
            dashboardStats.setUsedEmails(overallInstitutionStats.getEmails());
            dashboardStats.setUsedWhatsapp(overallInstitutionStats.getWhatsapp());

            SmsBalance smsBalance = this.smsService.getBalance();
            smsBalance.populateStats();
            dashboardStats.setSmsPromo(smsBalance.getPromo());
            dashboardStats.setSmsTrans(smsBalance.getTrans());
        } else {
            institution = this.appRepository.getInstitutionRepository()
                    .findById(loggedInUser.getInstitutionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Your institution is not recognized."));


            Map<String, Long> categoryWiseMemberStats = institution.getPartnerWiseStats(appRepository);
            dashboardStats.setPartnerCategoryWiseStats(categoryWiseMemberStats);

            SubscriptionPlan subscriptionPlan = GeneralValues.SUBSCRIPTIONS.get(institution.getSubscriptionPlan());
            if(subscriptionPlan==null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Failed to load dashboard stats.");
            }


            families = this.appRepository.getInstitutionFamilyRepository()
                    .countAllByInstitutionId(loggedInUser.getInstitutionId());
            dashboardStats.setFamilies(families);

            females = this.appRepository.getInstitutionMemberRepository()
                    .countAllByGenderAndInstitutionId(Gender.FEMALE, loggedInUser.getInstitutionId());
            males = this.appRepository.getInstitutionMemberRepository()
                    .countAllByGenderAndInstitutionId(Gender.MALE, loggedInUser.getInstitutionId());
            dashboardStats.setFemales(females);
            dashboardStats.setMales(males);

            final Institution theInstitution = institution;

            dashboardStats.setUsedSms(theInstitution.getSmses());
            dashboardStats.setMaxSms(subscriptionPlan.getSmses());

            dashboardStats.setUsedEmails(theInstitution.getEmails());
            dashboardStats.setMaxEmails(subscriptionPlan.getEmails());

            dashboardStats.setUsedWhatsapp(theInstitution.getWhatsapp());
            dashboardStats.setMaxWhatsapp(subscriptionPlan.getWhatsapp());


            List<UserType> userTypes = new ArrayList<>();
            if (theInstitution.getInstitutionType().equals(InstitutionType.CHURCH)) {
                userTypes.add(UserType.CHURCH_ADMINISTRATOR);
                userTypes.add(UserType.CHURCH_ASSISTANT_ADMINISTRATOR);
                userTypes.add(UserType.CHURCH_BRANCH_ADMINISTRATOR);
                userTypes.add(UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR);
            } else {
                userTypes.add(UserType.ORGANIZATION_ADMINISTRATOR);
                userTypes.add(UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR);
            }

            long adminsCount = this.appRepository.getUsersRepository()
                    .countAllByUserTypeInAndInstitutionId(userTypes, theInstitution.getId());
            admins.set(adminsCount);
            dashboardStats.setAdmins(admins.get());


            // Get expiration Message
            String expirationMessage = theInstitution.getExpirationMessage(false);
            if (!StringUtils.isEmpty(expirationMessage)) dashboardStats.setExpirationMessage(expirationMessage);


            this.computeWiseDonations(this.appRepository, theInstitution, dashboardStats);


            // get birthdays stats
            Date nextSevenDays = DateUtils.addDays(7);
            Long dobsInSevenDays = this.appRepository.getInstitutionMemberRepository()
                    .getBirthdaysBetween(today, nextSevenDays, institution.getId());
            dobsInSevenDays = (dobsInSevenDays==null) ? 0L : dobsInSevenDays;

            Long domsInSevenDays = this.appRepository.getInstitutionFamilyRepository()
                    .getDomsBetween(today, nextSevenDays, institution.getId());
            domsInSevenDays = (domsInSevenDays==null) ? 0L : domsInSevenDays;
            dashboardStats.setBirthdays(dobsInSevenDays);
            dashboardStats.setMarriages(domsInSevenDays);
        }

        
        
  
        
        InstitutionMember.addRegistrationStats(institution, this.getEntityManager(), dashboardStats);

        Institution.getGeneralStats(institution, this.getEntityManager(), appRepository, dashboardStats);
        if (institution == null) SubscriptionPlan.getStats(this.getEntityManager(), appRepository, dashboardStats);
        InstitutionMember.addGenderStats(institution, this.getEntityManager(), dashboardStats);

        if (institution == null) {
            Institution.getToppersStats(
                    this.getEntityManager(),
                    InstitutionType.CHURCH,
                    dashboardStats);

            Institution.getToppersStats(
                    this.getEntityManager(),
                    InstitutionType.GENERAL,
                    dashboardStats);
        } else {
            if (institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
                this.getOrganizationStats(loggedInUser, institution, dashboardStats);
            } else {
                this.getChurchStats(loggedInUser, institution, dashboardStats);
            }


            Map<DashboardDuration, List<TopMember>> topMembersMap = this.getTopMembersMap(institution, today);
            if (!topMembersMap.isEmpty()) dashboardStats.setInstitutionWiseTopMembers(topMembersMap);

            long activeCount = this.appRepository
                    .getInstitutionMemberRepository()
                    .countAllByInstitutionIdAndActive(institution.getId(), true);
            long inactiveCount = this.appRepository
                    .getInstitutionMemberRepository()
                    .countAllByInstitutionIdAndActive(institution.getId(), false);
            dashboardStats.setActive(activeCount);
            dashboardStats.setInactive(inactiveCount);


            long membersInIndia = this.appRepository
                    .getInstitutionMemberRepository()
                    .countAllByCountryIsAndInstitutionIdIs("IN",
                            institution.getId());
            long membersAbroad = this.appRepository
                    .getInstitutionMemberRepository()
                    .countAllByCountryIsNotAndInstitutionIdIs("IN",
                            institution.getId());
            dashboardStats.setMembersInIndia(membersInIndia);
            dashboardStats.setMembersAbroad(membersAbroad);
        }
        return dashboardStats;
    }



    private Map<DashboardDuration, List<TopMember>> getTopMembersMap(@NonNull Institution institution,
                                                                     @NonNull final Date today) {
        final Institution theInstitution = institution;
        final List<DashboardDuration> dashboardDurations = Arrays.asList(DashboardDuration.values());
        Map<DashboardDuration, List<TopMember>> topMembersMap = new HashMap<>();
        dashboardDurations
                .parallelStream()
                .forEach(dashboardDuration -> {
                    DateGap startingDate = DashboardUtils.getDateGap(dashboardDuration);
                    PageRequest pageRequest = PageRequest.of(0, 5,
                            Sort.by(Sort.Direction.DESC, TopMember.Fields.amount));
                    List<TopMember> topMembersDonations = institution.getInstitutionType().equals(InstitutionType.GENERAL)
                            ? this.appRepository.getInstitutionDonationRepository().getTopDonationsInGeneral(theInstitution.getId(), startingDate.getFrom(), today, pageRequest)
                            : this.appRepository.getInstitutionDonationRepository().getTopDonationsInChurch(theInstitution.getId(), startingDate.getFrom(), today, pageRequest);
                    topMembersMap.put(dashboardDuration, topMembersDonations);
        });
        return topMembersMap;
    }


    private void getChurchStats(@NonNull User loggedInUser,
                                @NonNull Institution institution,
                                @NonNull DashboardStats stats) {
        List<UserType> usersAllowedToViewDonations = User.getUserTypesExcept(
                Arrays.asList(UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
                        UserType.CHURCH_DATA_ENTRY_OPERATOR));
        if (!usersAllowedToViewDonations.contains(loggedInUser.getUserType())) {
            // get offerings
            Map<DashboardDuration, Map<String, Long>> totalDonationsMap = this.computeInstitutionDonations(institution);
            if (!totalDonationsMap.isEmpty()) stats.setInstitutionWiseDonation(totalDonationsMap);
        }

        long activeMembers = appRepository.getInstitutionMemberRepository()
                .countAllByActiveAndInstitutionId(true, institution.getId()),
                inactiveMembers = appRepository.getInstitutionMemberRepository()
                        .countAllByActiveAndInstitutionId(false, institution.getId()),


                baptizedMale = appRepository.getInstitutionMemberRepository()
                        .countAllByBaptizedAndGenderAndInstitutionId(BaptismStatus.BAPTIZED, Gender.MALE, institution.getId()),
                baptizedFemale = appRepository.getInstitutionMemberRepository()
                        .countAllByBaptizedAndGenderAndInstitutionId(BaptismStatus.BAPTIZED, Gender.FEMALE, institution.getId()),


                communionMale = appRepository.getInstitutionMemberRepository()
                        .countAllByCommunionAndGenderAndInstitutionId(true, Gender.MALE, institution.getId()),
                communionFemale = appRepository.getInstitutionMemberRepository()
                        .countAllByCommunionAndGenderAndInstitutionId(true, Gender.FEMALE, institution.getId());

        stats.setActiveMembers(activeMembers);
        stats.setInactiveMembers(inactiveMembers);
        stats.setBaptizedMale(baptizedMale);
        stats.setBaptizedFemale(baptizedFemale);
        stats.setCommunionMale(communionMale);
        stats.setCommunionFemale(communionFemale);

        DateGap babiesDateGap = DateUtils.getDateGapsBetweenYears(0, 6),
                childrenDateGap = DateUtils.getDateGapsBetweenYears(6, 13),
                teenageDateGap = DateUtils.getDateGapsBetweenYears(13, 20),
                youthDateGap = DateUtils.getDateGapsBetweenYears(20, 30),
                adultDateGap = DateUtils.getDateGapsBetweenYears(30, 200);

        long babiesMale = appRepository.getInstitutionMemberRepository()
                .getGenderBasedDobs(babiesDateGap.getFrom(), babiesDateGap.getEnd(), Gender.MALE, institution.getId()),
                babiesFemale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(babiesDateGap.getFrom(), babiesDateGap.getEnd(), Gender.FEMALE, institution.getId()),

                childrenMale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(childrenDateGap.getFrom(), childrenDateGap.getEnd(), Gender.MALE, institution.getId()),
                childrenFemale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(childrenDateGap.getFrom(), childrenDateGap.getEnd(), Gender.FEMALE, institution.getId()),

                teenageMale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(teenageDateGap.getFrom(), teenageDateGap.getEnd(), Gender.MALE, institution.getId()),
                teenageFemale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(teenageDateGap.getFrom(), teenageDateGap.getEnd(), Gender.FEMALE, institution.getId()),

                youthMale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(youthDateGap.getFrom(), youthDateGap.getEnd(), Gender.MALE, institution.getId()),
                youthFemale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(youthDateGap.getFrom(), youthDateGap.getEnd(), Gender.FEMALE, institution.getId()),

                adultMale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(adultDateGap.getFrom(), adultDateGap.getEnd(), Gender.MALE, institution.getId()),
                adultFemale = appRepository.getInstitutionMemberRepository()
                        .getGenderBasedDobs(adultDateGap.getFrom(), adultDateGap.getEnd(), Gender.FEMALE, institution.getId());

        stats.setBabiesMale(babiesMale);
        stats.setBabiesFemale(babiesFemale);
        stats.setChildrenMale(childrenMale);
        stats.setChildrenFemale(childrenFemale);
        stats.setTeenageMale(teenageMale);
        stats.setTeenageFemale(teenageFemale);
        stats.setYouthMale(youthMale);
        stats.setYouthFemale(youthFemale);
        stats.setAdultMale(adultMale);
        stats.setAdultFemale(adultFemale);
    }

    private Map<DashboardDuration, Map<String, Long>> computeInstitutionDonations(@NonNull Institution institution) {
        Map<DashboardDuration, Map<String, Long>> totalDonationsMap = new HashMap<>();
        final Date today = DateUtils.atTheEndOfTheDay(DateUtils.getNowDateTime());
        if(institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            List<ChurchContribution> contributions = this.appRepository.getChurchContributionRepository()
                    .findAllByInstitutionId(institution.getId());
            if(contributions!=null && !contributions.isEmpty()){
                this.dashboardDurations
                        .parallelStream()
                        .forEach(dashboardDuration -> {
                            DateGap startingDate = DashboardUtils.getDateGap(dashboardDuration);
                            Map<String, Long> contributionsPerDuration = new HashMap<>();
                            contributions.parallelStream()
                                    .forEach(contribution->{
                                        List<UniqueDonation> uniqueDonations = this.appRepository
                                                .getInstitutionDonationRepository()
                                                .getChurchUniqueDonations(contribution.getId(),
                                                        startingDate.getFrom(),
                                                        today);
                                        AtomicReference<Long> totalDonations = new AtomicReference<>(0L);
                                        uniqueDonations.parallelStream()
                                                        .forEach(uniqueDonation -> totalDonations.updateAndGet(v -> v + uniqueDonation.getAmount()));
                                        contributionsPerDuration.put(contribution.getName(), totalDonations.get());
                                    });
                            if (!contributionsPerDuration.isEmpty()) {
                                totalDonationsMap.put(dashboardDuration, contributionsPerDuration);
                            }
                        });
            }
        } else {
            List<ChurchContribution> contributions = this.appRepository.getChurchContributionRepository()
                    .findAllByInstitutionId(institution.getId());
            if (contributions != null && !contributions.isEmpty()) {
                this.dashboardDurations
                        .parallelStream()
                        .forEach(dashboardDuration -> {
                            DateGap startingDate = DashboardUtils.getDateGap(dashboardDuration);
                            Map<String, Long> contributionsPerDuration = new HashMap<>();
                            contributions.parallelStream()
                                    .forEach(contribution -> {
                                        List<UniqueDonation> uniqueDonations = this.appRepository
                                                .getInstitutionDonationRepository()
                                                .getGOUniqueDonations(contribution.getId(),
                                                        startingDate.getFrom(),
                                                        today);
                                       AtomicReference<Long> totalDonations = new AtomicReference<>(0L);
                                        uniqueDonations.parallelStream()
                                                        .forEach(uniqueDonation -> totalDonations.updateAndGet(v -> v + uniqueDonation.getAmount()));
                                        contributionsPerDuration.put(contribution.getName(), totalDonations.get());
                                    });
                            if (!contributionsPerDuration.isEmpty()) {
                                totalDonationsMap.put(dashboardDuration, contributionsPerDuration);
                            }
                        });
            }
        }
        return totalDonationsMap;
    }


    private void getOrganizationStats(@NonNull User loggedInUser,
                                      @Nullable Institution institution,
                                      @NonNull DashboardStats stats) {
        // get offerings
        List<InstitutionCategory> contributions = institution != null
                ? this.appRepository.getCategoryRepository()
                .findAllByInstitutionId(institution.getId())
                : null;
        Date today = DateUtils.atTheEndOfTheDay(DateUtils.getNowDateTime());
        List<DashboardDuration> dashboardDurations = Arrays.asList(DashboardDuration.values());

        List<UserType> usersAllowedToViewDonations = User.getUserTypesExcept(
                Arrays.asList(UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
                        UserType.CHURCH_DATA_ENTRY_OPERATOR));

        if (!usersAllowedToViewDonations.contains(loggedInUser.getUserType())) {
            Map<DashboardDuration, Map<String, Long>> totalDonationsMap = new HashMap<>();
            dashboardDurations
                    .parallelStream()
                    .forEach(dashboardDuration -> {
                        DateGap startingDate = DashboardUtils.getDateGap(dashboardDuration, null);
                        Map<String, Long> contributionsPerDuration = new HashMap<>();
                        if (contributions != null) {
                            contributions.parallelStream()
                                    .forEach(contribution -> {
                                        Long totalDonations = InstitutionDonation
                                                .getOrganizationSumDonations(this.getEntityManager(),
                                                        contribution.getId(),
                                                        institution.getId(),
                                                        startingDate.getFrom(),
                                                        today);
                                        contributionsPerDuration.put(contribution.getName(), totalDonations);
                                    });
                            if (!contributionsPerDuration.isEmpty()) {
                                totalDonationsMap.put(dashboardDuration, contributionsPerDuration);
                            }
                        }
            });
            if (!totalDonationsMap.isEmpty()) stats.setInstitutionWiseDonation(totalDonationsMap);
        }
    }

    private void computeWiseDonations(@NonNull AppRepository appRepository,
                                      @NonNull Institution institution,
                                      @NonNull DashboardStats stats){
        Map<DashboardDuration, Map<String, Long>> durationBasedDuration = new HashMap<>();
        Map<DashboardDuration, Map<String, Long>> durationBasedDurationMembers = new HashMap<>();
        List<DashboardDuration> dashboardDurations = Arrays
                .asList(DashboardDuration.values());
        dashboardDurations.parallelStream()
                .forEach(duration->{
                    DateGap dateGap = DashboardUtils.getDateGap(duration, null);
                    CategorizedStats categorizedStats = this.addCategoryWiseDonations(
                            appRepository,
                            institution, dateGap
                    );
                    if(categorizedStats.getDonations()!=null
                            && !categorizedStats.getDonations().isEmpty()) {
                        durationBasedDuration.put(duration, categorizedStats.getDonations());
                    }
                    if(categorizedStats.getMembersByCategory()!=null
                            && !categorizedStats.getMembersByCategory().isEmpty()) {
                        durationBasedDurationMembers.put(duration, categorizedStats.getMembersByCategory());
                    }
                });
        if(!durationBasedDuration.isEmpty()) stats.setCategoryWiseDonations(durationBasedDuration);
        if(!durationBasedDurationMembers.isEmpty()) stats.setMembersByCategory(durationBasedDurationMembers);
    }

    private CategorizedStats addCategoryWiseDonations(@NonNull AppRepository appRepository,
                                                       @NonNull Institution institution,
                                                       @NonNull DateGap dateGap){
        Map<String, Long> donations = new HashMap<>();
        Map<String, Long> membersByCategory = new HashMap<>();

        Map<Long, String> categoriesMap = institution.getInstitutionType().equals(InstitutionType.GENERAL)
                ? appRepository.getCategoryRepository().findAllByInstitutionId(institution.getId())
                    .parallelStream()
                    .collect(Collectors.toMap(InstitutionCategory::getId, InstitutionCategory::getName))
                : appRepository.getChurchContributionRepository().findAllByInstitutionId(institution.getId())
                    .parallelStream()
                    .collect(Collectors.toMap(ChurchContribution::getId, ChurchContribution::getName));

        if(institution.getInstitutionType().equals(InstitutionType.GENERAL)) {
            categoriesMap.keySet()
                    .parallelStream()
                    .forEach(categoryId->{
                        List<UniqueDonation> uniqueDonations = appRepository.getInstitutionDonationRepository()
                                .sumAllDonationsByCategory(categoryId, dateGap.getFrom(), dateGap.getEnd());
                        AtomicReference<Long> total = new AtomicReference<>(0L);
                        uniqueDonations.parallelStream()
                                .forEach(singleUniqueDonation->total.updateAndGet(v -> v + singleUniqueDonation.getAmount()));
                        String categoryName = categoriesMap.get(categoryId);
                        donations.put(categoryName.toUpperCase(), total.get());

                        // Look for all the members in particular
                        Long membersCount = appRepository.getInstitutionMemberRepository()
                                .countWhereCategoriesUsed(categoryId.toString(), dateGap.getFrom(), dateGap.getEnd());
                        membersCount = membersCount==null ? 0 : membersCount;
                        membersByCategory.put(categoryName.toUpperCase(), membersCount);
                    });
        } else {
            categoriesMap.keySet()
                    .parallelStream()
                    .forEach(contributionId->{
                        List<UniqueDonation> uniqueDonations = appRepository.getInstitutionDonationRepository()
                                .sumAllDonationsByContribution(contributionId, dateGap.getFrom(), dateGap.getEnd());
                        AtomicReference<Long> total = new AtomicReference<>(0L);
                        uniqueDonations.parallelStream()
                                .forEach(singleUniqueDonation->total.updateAndGet(v -> v + singleUniqueDonation.getAmount()));
                        String contributionName = categoriesMap.get(contributionId);
                        donations.put(contributionName.toUpperCase(), total.get());

                        // Look for all the members in particular
                        Long membersCount = appRepository.getInstitutionMemberRepository()
                                .countWhereContributionUsed(contributionId.toString(), dateGap.getFrom(), dateGap.getEnd());
                        membersCount = membersCount==null ? 0 : membersCount;
                        membersByCategory.put(contributionName.toUpperCase(), membersCount);
                    });
        }

        CategorizedStats stats = new CategorizedStats();
        if(!donations.isEmpty()) stats.setDonations(donations);
        if(!membersByCategory.isEmpty()) stats.setMembersByCategory(membersByCategory);
        return stats;
    }
}
