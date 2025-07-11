package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.entities.Institution;
import com.dprince.entities.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DonationListQuery extends ListQuery implements Institutionable {
    private Long institutionId;

    private Long memberId;

    private PaymentMode paymentMode;

    private Long category;

    private String receiptNo;

    @JsonIgnore
    private Institution institution;
}
