package com.dprince.apis.institution.models;

import com.dprince.entities.enums.Subscription;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionRenewal extends PlanUpgrader {
    @NotNull(message = "Please select the subscription duration.")
    private Subscription subscription;
}
