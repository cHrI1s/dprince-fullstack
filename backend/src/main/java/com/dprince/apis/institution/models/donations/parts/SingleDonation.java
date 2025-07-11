package com.dprince.apis.institution.models.donations.parts;

import com.dprince.apis.institution.validators.ChurchContributionValidator;
import com.dprince.apis.institution.validators.GeneralDonationValidation;
import com.dprince.entities.enums.Subscription;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SingleDonation {
    @NotNull(message = "Please specify the category paid to.",
            groups = { GeneralDonationValidation.class })
    private Long categoryId;


    @NotNull(message = "Please specify which contribution was made.",
            groups = { ChurchContributionValidator.class })
    private Long contributionId;

    @NotNull(message = "Amount not specified.",
            groups = {
                ChurchContributionValidator.class,
                GeneralDonationValidation.class
    })
    @Min(value = 5, message = "The minimum amount is 5",
            groups = {
                ChurchContributionValidator.class,
                GeneralDonationValidation.class
    })
    private Long amount;

    @NotNull(message = "Please specify the subscription timing",
            groups = {
            GeneralDonationValidation.class
    })
    private Subscription subscription;
}
