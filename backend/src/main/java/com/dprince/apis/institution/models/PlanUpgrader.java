package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PlanUpgrader implements Institutionable {
    @NotNull(message = "Please provide the institution to upgrade plan.")
    private Long institutionId;

    @NotNull(message = "Please select a plan to upgrade to.")
    private Long planId;
}
