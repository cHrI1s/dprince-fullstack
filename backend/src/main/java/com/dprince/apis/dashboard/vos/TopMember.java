package com.dprince.apis.dashboard.vos;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Data
public class TopMember {
    private String code;
    private String fullName;
    private String category;
    private Long amount;
    private String district;

    public TopMember(){}

    public TopMember(String code,
                     String fullName,
                     String category,
                     Long amount,
                     String district){
        this.code = code;
        this.fullName = fullName;
        this.category = category;
        this.amount = amount;
        this.district = district;
    }
}
