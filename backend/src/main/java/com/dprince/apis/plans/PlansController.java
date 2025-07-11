package com.dprince.apis.plans;

import com.dprince.apis.plans.models.SubscriptionDeletionModel;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.Institution;
import com.dprince.entities.Notification;
import com.dprince.entities.SubscriptionPlan;
import com.dprince.entities.User;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.startup.GeneralValues;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/plan")
public class PlansController {
    private final AppRepository appRepository;

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save")
    public ApiWorkFeedback saveSubscriptionPlan(@RequestBody @Valid SubscriptionPlan subscriptionPlan,
                                                @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        boolean isUpdate = subscriptionPlan.getId()!=null;
        if(isUpdate){
            SubscriptionPlan subscriptionPlan1 = this.appRepository.getSubscriptionPlanRepository()
                    .findById(subscriptionPlan.getId()).orElse(null);
            if(subscriptionPlan1==null){
                throw  new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "The subscription plan does not exist.");
            }
        }

        SubscriptionPlan save = this.appRepository.getSubscriptionPlanRepository()
                .save(subscriptionPlan);
        GeneralValues.addSubscriptions(save);
        String message = (isUpdate) ? "Updated." : "Saved.";
        String notificationTitle = "New Subscription Plan Saved";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                save.getName(),
                NotificationActionType.UPDATE,
                null
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback
                .builder()
                .successful(true)
                .message(message)
                .object(save)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/get-all/{institutionType}")
    public List<SubscriptionPlan> getPlans(@PathVariable("institutionType") InstitutionType institutionType) {
        return this.appRepository
                .getSubscriptionPlanRepository()
                .findAllByInstitutionType
                (institutionType);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/get-all")
    public List<SubscriptionPlan> getPlans() {
        return new ArrayList<>(GeneralValues.SUBSCRIPTIONS.values());
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/delete")
    public ApiWorkFeedback deletePlan(@RequestBody
                                       @Valid SubscriptionDeletionModel subscriptionDeletionModel,
                                      @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        Institution savedInstitution = this.appRepository.getInstitutionRepository()
                .findInstitutionBySubscriptionPlan(subscriptionDeletionModel.getSubscriptionPlan());
        if(savedInstitution!=null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "This subscription is in use it cannot be deleted.");
        }
        SubscriptionPlan plan = this.appRepository
                .getSubscriptionPlanRepository()
                .findById(subscriptionDeletionModel.getId())
                .orElse(null);
        if(plan==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The Subscription plan is not recognized.");
        }

        this.appRepository.getSubscriptionPlanRepository()
                .deleteById(subscriptionDeletionModel.getId());
        String notificationTitle = "Subscription Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                plan.getName(),
                NotificationActionType.UPDATE,
                null
        );
        GeneralValues.SUBSCRIPTIONS.remove(subscriptionDeletionModel.getId());
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback
                .builder()
                .message("Deleted.")
                .successful(true)
                .build();
    }
}

