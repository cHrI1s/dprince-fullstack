package com.dprince.entities.embeded;

import com.dprince.apis.institution.models.validations.ChurchMemberCreationValidator;
import com.dprince.apis.institution.models.validations.PartnerCreationValidator;
import com.dprince.apis.institution.models.validations.ProspectusMemberValidation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import org.apache.commons.lang3.StringUtils;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;


@Data
@MappedSuperclass
public class PersonAddress {
	
	
    @NotBlank(message = "Address line 1 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Column(nullable = false)
    public String addressLine1;
    public void setAddressLine1(@NotBlank(message = "Address line 1 must not be empty.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    }) String addressLine1) {
        this.addressLine1 = addressLine1.trim();
    }

    @NotBlank(message = "Address line 2 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Column(nullable = false)
    public String addressLine2;
    public void setAddressLine2(@NotBlank(message = "Address line 2 must not be empty.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    }) String addressLine2) {
        this.addressLine2 = addressLine2.trim();
    }

    @NotBlank(message = "Address line 3 must not be empty.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Column(nullable = false)
    public String addressLine3;
    public void setAddressLine3(@NotBlank(message = "Address line 3 must not be empty.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    }) String addressLine3) {
        this.addressLine3 = addressLine3.trim();
    }

    @Column(length = 6, nullable = false)
    public Integer pincode;

    @NotBlank(message = "The district must be provided.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Column(nullable = false, length = 40)
    public String district;
    public void setDistrict(@NotBlank(message = "The district must be provided.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    }) String district) {
        this.district = district.trim();
    }

    @NotBlank(message = "The state must be provided.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Column(nullable = false)
    public String state;
    public void setState(@NotBlank(message = "The state must be provided.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    }) String state) {
        this.state = state.trim();
    }

    @NotBlank(message = "The country must be provided.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    })
    @Column(length = 2, nullable = false)
    public String country;
    public void setCountry(@NotBlank(message = "The country must be provided.", groups = {
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class
    }) String country) {
        this.country = country.trim();
    }

    @JsonIgnore
    public String getFullAddress(){
        List<String> addressList = new ArrayList<>();
        if(!StringUtils.isEmpty(this.getAddressLine1())) addressList.add(this.getAddressLine1());
        if(!StringUtils.isEmpty(this.getAddressLine2())) addressList.add(this.getAddressLine2());
        if(!StringUtils.isEmpty(this.getAddressLine3())) addressList.add(this.getAddressLine3());
        if(!StringUtils.isEmpty(this.getDistrict())) addressList.add(this.getDistrict());
        if(!StringUtils.isEmpty(this.getState())) addressList.add(this.getState());
        if(!StringUtils.isEmpty(this.getCountry())) addressList.add(this.getCountry());
        return String.join(", ", addressList);
    }

    /*
    @NotNull(message = "Phone must not be empty", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })*/
    @Max(value = 999999999999L,
            message = "The number must not exceed 12 digit number.", groups={
            PartnerCreationValidator.class,
            ChurchMemberCreationValidator.class,
            ProspectusMemberValidation.class
    })
    @Positive(message="The phone number must be a positive number.")
    @Column(length = 12)
    public Long phone;

    @NotNull(message = "Code must not be empty.",
            groups={
                PartnerCreationValidator.class,
                ChurchMemberCreationValidator.class,
                ProspectusMemberValidation.class
            })
    public Long phoneCode = 91L;

    @Positive(message="The alternate phone number must be a positive number.")
    @Column(length = 12)
    public Long alternatePhone;
    public Long alternatePhoneCode;

    @Positive(message="The landline phone number must be a positive number.")
    @Column(length = 12)
    public Long landlinePhone;
    public Long landlinePhoneCode = 91L;


    @Positive(message="The whatsapp number must be a positive number.")
    @Column(length = 12)
    public Long whatsappNumber;
    public Long whatsappNumberCode;

    @Transient
    @JsonIgnore
    private String fullWhatsappNumber;
    public String getFullWhatsappFullNumber(){
        if(!StringUtils.isEmpty(this.fullWhatsappNumber)){
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(this.fullWhatsappNumber, "");
                int countryCode = phoneNumber.getCountryCode();
                long phoneNumberPart = phoneNumber.getNationalNumber();
                this.setWhatsappNumberCode((long) countryCode);
                this.setWhatsappNumber(phoneNumberPart);
                return this.fullWhatsappNumber;
            } catch (Exception ignored){}
        }
        if(this.getWhatsappNumber()!=null){
            String output = "+"+((this.getWhatsappNumberCode()==null) ? 91 : this.getWhatsappNumberCode());
            output += this.getWhatsappNumber();
            return output;
        }
        return null;
    }
}
