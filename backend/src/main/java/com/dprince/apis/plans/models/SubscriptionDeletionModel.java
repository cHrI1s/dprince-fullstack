package com.dprince.apis.plans.models;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SubscriptionDeletionModel  {
    @NotNull(message = "Subscription plan must be specified.")
    private Long id;

    @NotNull(message = "THe institution must be institution specified.")
    private Long subscriptionPlan;
}
