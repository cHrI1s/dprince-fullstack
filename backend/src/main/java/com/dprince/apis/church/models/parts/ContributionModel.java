package com.dprince.apis.church.models.parts;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ContributionModel {
    @NotNull(message = "Please specify which contribution was made.")
    private Long contributionId;

    @NotNull(message = "Amount not specified.")
    @Min(value = 5, message = "The minimum amount is 5")
    private Long amount;
}
