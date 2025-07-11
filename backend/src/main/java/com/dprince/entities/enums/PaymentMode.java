package com.dprince.entities.enums;

import lombok.Getter;

@Getter
public enum PaymentMode {
    CASH("Cash"),
    UPI("UPI"),
    MO("MO"),
    CHEQUE("Cheque"),
    DD("DD"),
    ONLINE_DONATION("Online Donation");

    private final String displayName;
    PaymentMode(String displayName){
        this.displayName = displayName;
    }

}
