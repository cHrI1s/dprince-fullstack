package com.dprince.startup;

import com.dprince.entities.Institution;
import com.dprince.entities.PersonTitle;
import com.dprince.entities.SubscriptionPlan;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GeneralValues {
    public static Map<Long, PersonTitle> PERSON_TITLES = new HashMap<>();
    public static void addPersonTitles(List<PersonTitle> titles){
        titles.parallelStream()
                .forEach(singleTitle-> PERSON_TITLES.put(singleTitle.getId(), singleTitle));
    }

    public static Map<Long, SubscriptionPlan> SUBSCRIPTIONS = new HashMap<>();
    public static void addSubscriptions(List<SubscriptionPlan> plans){
        plans.parallelStream()
                .forEach(singlePlan-> SUBSCRIPTIONS.put(singlePlan.getId(), singlePlan));
    }
    public static void deleteSubscription(SubscriptionPlan plan){
        SUBSCRIPTIONS.remove(plan.getId());
    }
    public static void addSubscriptions(SubscriptionPlan plan){
        SUBSCRIPTIONS.put(plan.getId(), plan);
    }

    public static Map<Long, Institution> INSTITUTIONS = new HashMap<>();
    public static void addInstitution(Institution institution){
        INSTITUTIONS.put(institution.getId(), institution);
    }
    public static void removeInstitution(Institution institution){
        INSTITUTIONS.remove(institution.getId());
    }

    public static void initialize(AppRepository appRepository){
        PERSON_TITLES = appRepository.getPersonTitlesRepository()
                .findAll()
                .parallelStream()
                .collect(Collectors.toMap(PersonTitle::getId, title->title));

        // Institutions
        SUBSCRIPTIONS = appRepository.getSubscriptionPlanRepository()
                .findAll()
                .parallelStream()
                .collect(Collectors.toMap(SubscriptionPlan::getId, plan->plan));

        // Institutions
        INSTITUTIONS = appRepository.getInstitutionRepository()
                .findAll()
                .parallelStream()
                .collect(Collectors.toMap(Institution::getId, institution->institution));
    }
}
