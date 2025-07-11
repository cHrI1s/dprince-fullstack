package com.dprince.apis.misc.receipt.model;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.ReceiptTemplate;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Data
public class ReceiptModel implements Institutionable {
    private Long institutionId;

    //@NotBlank(message = "The bible verse must not be empty.")
    private String bibleVerse;

    //@NotBlank(message = "The message must not be empty.")
    private String receiptMessage;

    private Long receiptPhone;

    private ReceiptTemplate receiptTemplate;

    private String upi;

    private String customReceiptTemplate;

    private ReceiptTemplate customModel;


    public void check(){
        if(this.getReceiptTemplate()==null){
            if(StringUtils.isEmpty(this.getCustomReceiptTemplate()) || this.getCustomModel()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Make sure the required data for custom template are provided.");
            }
            this.receiptTemplate = null;
            this.receiptPhone = null;
        } else {
            this.customReceiptTemplate = null;
            this.customModel = null;
            if(this.getReceiptPhone()==null){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Phone number must not be empty.");
            } else {
                if(this.getReceiptPhone() < 0){
                    throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "The phone number must be a positive number.");
                }
            }
        }
    }

}
