package com.dprince.entities.embeded;

import com.dprince.apis.institution.models.validations.ChurchMemberCreationValidator;
import com.dprince.apis.institution.models.validations.PartnerCreationValidator;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@MappedSuperclass
public class FamilyAddress {
    @NotBlank(message = "Address line 1 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false)
    public String addressLine1;

    @NotBlank(message = "Address line 2 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false)
    public String addressLine2;


    @NotBlank(message = "Address line 3 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(nullable = false)
    public String addressLine3;

    @Column(length = 6)
    public Integer pincode;

    @Column(nullable = false, length = 40)
    public String district;

    @Column(nullable = false, length = 100)
    public String state;

    @Column(nullable = false, length = 100)
    private String country = "IN";

    @NotNull(message = "Phone must not be empty", groups={
            PartnerCreationValidator.class
    })
    @Column(length = 12)
    public Long phone;


    @Column(length = 4)
    public Long phoneCode = 91L;
}
