package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class ReceiptRequester implements Institutionable {
    private String receiptCode;
    private Long institutionId;
}
