package com.dprince.entities.vos;

import com.dprince.entities.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class SingleDonationReport {
    private String receiptNo;
    private Date donationDate;
    private String memberCode;
    private String donator;
    private PaymentMode paymentMode;
    private Long amount;
}
