package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.enums.DurationGap;
import com.dprince.entities.enums.Subscription;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemberListingQuery extends AdminListingQuery {
    private DurationGap creationDateIn = DurationGap.TODAY;
    private List<Long> categories;
    private String district;
    private Integer pincode;
    private Long alternatePhone;
    private Long alternatePhoneCode;
    private Long phone;
    private Long phoneCode;
    private Subscription subscription;
    private List<String> partnerCodes;
    private List<String> countries;
    private List<String> states;
    private String state;
    private Boolean active;
}
