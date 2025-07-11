package com.dprince.entities.vos;

import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DonationReport {
    private Long id;
    private Long phone;
    private Long phoneCode;
    private String receiptNo;
    private Date donationDate;
    private String memberCode;
    private String donator;
    private String category;
    private Set<String> categories;
    public void addCategory(String category){
        if(StringUtils.isEmpty(category)) return;
        if(this.categories==null) this.categories = new HashSet<>();
        this.categories.add(category);
        this.category = String.join(", ", this.getCategories());
    }
    private PaymentMode paymentMode;
    private Long amount;
    public void addAmount(@NonNull Long amount){
        if(this.amount==null) this.amount = 0L;
        this.amount += amount;
    }

    @JsonIgnore
    private List<Long> categoriesIds;

    public DonationReport(Long id,
                          String receiptNo,
                          Date donationDate,
                          String memberCode,
                          String donator,
                          PaymentMode paymentMode,
                          Long amount) {
        this.id = id;
        this.receiptNo = receiptNo;
        this.donationDate = donationDate;
        this.memberCode = memberCode;
        this.donator = donator;
        this.paymentMode = paymentMode;
        this.amount = amount;
    }

    public DonationReport(Long id,
                          String receiptNo,
                          Date donationDate,
                          String memberCode,
                          String donator,
                          String category,
                          PaymentMode paymentMode,
                          Long amount) {
        this.id = id;
        this.receiptNo = receiptNo;
        this.donationDate = donationDate;
        this.memberCode = memberCode;
        this.donator = donator;
        this.category = category;
        this.paymentMode = paymentMode;
        this.amount = amount;
    }

    public DonationReport(Long id,
                          Date donationDate,
                          String memberCode,
                          String donator,
                          String category,
                          Gender gender) {
        this.id = id;
        this.receiptNo = receiptNo;
        this.donationDate = donationDate;
        this.memberCode = memberCode;
        this.donator = donator;
        this.category = category;
        this.gender = gender;
    }

    public DonationReport(Long id,
                          Long phoneCode,
                          Long phone,
                          String memberCode,
                          String donator,
                          Gender gender) {
        this.id = id;
        this.receiptNo = receiptNo;
        this.phone = phone;
        this.phoneCode = phoneCode;
        this.memberCode = memberCode;
        this.donator = donator;
        this.gender = gender;
    }

    private Gender gender;
}
