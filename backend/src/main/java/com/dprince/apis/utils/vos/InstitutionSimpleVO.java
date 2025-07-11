package com.dprince.apis.utils.vos;

import com.dprince.entities.enums.ReceiptTemplate;
import lombok.Data;

@Data
public class InstitutionSimpleVO {
    private String name;
    private String email;
    private Long phone;
    private Long receiptPhone;
    private String address;
    private String logo;
    private ReceiptTemplate receiptTemplate = ReceiptTemplate.TEMPLATE_2;
    public ReceiptTemplate getReceiptTemplate() {
        return receiptTemplate==null ? ReceiptTemplate.TEMPLATE_2 : receiptTemplate;
    }

    private String upi;
    private String customReceiptTemplate;
    private ReceiptTemplate customModel;

    private String bibleVerse;
    private String receiptMessage;
}
