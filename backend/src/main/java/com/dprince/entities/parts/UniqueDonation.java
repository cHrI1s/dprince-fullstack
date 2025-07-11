package com.dprince.entities.parts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class UniqueDonation {
    private Long amount;
    private String receiptNo;
    private Date entryDate;
}
