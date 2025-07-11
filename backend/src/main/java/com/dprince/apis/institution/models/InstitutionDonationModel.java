package com.dprince.apis.institution.models;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Data
public class InstitutionDonationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    private Long institutionId;

    @NotNull(message = "Member should be specified.")
    private Long memberId;

    private Long churchContributionId;

    @NotNull(message = "Payment mode should be specified.")
    public PaymentMode paymentMode;

    @NotNull(message = "Payment category should be specified.")
    public Long category;

    @NotNull(message = "Please provide the amount.")
    @Min(value = 1, message = "At least one Rupee(s) should be donated.")
    public Long amount;

    public String remarks;

    public String referenceNo;

    public String bankReference;

    public String referenceAccount;

    private Date entryDate = DateUtils.getNowDateTime();
}
