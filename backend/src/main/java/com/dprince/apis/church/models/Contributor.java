package com.dprince.apis.church.models;

import com.dprince.apis.church.models.parts.ContributionModel;
import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.PaymentMode;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class Contributor {
    @NotNull(message = "Please specify the contributor.")
    private Long memberId;

    @NotNull(message = "Please provide the payment mode.")
    private PaymentMode paymentMode;

    @PastOrPresent(message = "The contribution date must be a today or a date in the past.")
    private Date entryDate = DateUtils.getNowDateTime();

    private Long creditAccountId;

    @Valid
    @Size(min = 1, message = "At list one contribution should be made")
    private List<ContributionModel> contributions;

    private String bankReference;
    private String referenceNo;
    private String referenceAccount;
    private String remarks;
}
