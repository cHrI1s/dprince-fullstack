package com.dprince.entities.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum CertificateType {
    BIRTHDAY("BDAY", "Birthday"),
    MARRIAGE("MRGE", "Marriage"),
    KID_DEDICATION("KIDD", "Child Dedication"),
    BAPTISM("BPTM", "Baptism"),
    MEMBERSHIP("MSHIP", "Membership");

    private final String code;
    private final String label;
    CertificateType(String code, String label){
        this.code = code;
        this.label = label;
    }

    public static List<CertificateType> getAll(){
        return Arrays.asList(CertificateType.values());
    }
}
