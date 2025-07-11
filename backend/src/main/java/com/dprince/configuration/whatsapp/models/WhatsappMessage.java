package com.dprince.configuration.whatsapp.models;

import com.dprince.configuration.whatsapp.models.part.WhatsappMessageData;
import com.dprince.entities.Institution;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * @description: This Class is used to send a whatsapp Message to people.
 *
 */
@NoArgsConstructor
@Data
public class WhatsappMessage {
    private static final Logger log = LogManager.getLogger(WhatsappMessage.class);

    public WhatsappMessage(@Nullable Long countryCode,
                           @NonNull Long phoneNumber){
        long whatsappCode = countryCode == null ? 91L : countryCode;
        this.setCustomerCountryCode(whatsappCode+"");
        this.setCustomerNumber(phoneNumber.toString());
    }

    @JsonIgnore
    private Institution institution;

    @JsonProperty("customer_country_code")
    private String customerCountryCode;

    @JsonProperty("phone_number_id")
    private String phoneNumberId;

    @JsonProperty("customer_number")
    private String customerNumber;

    @JsonProperty("reply_to")
    private String replyTo;

    @JsonProperty("myop_ref_id")
    private String myOpRefId;

    @JsonProperty("data")
    private WhatsappMessageData data;

    @JsonIgnore
    private Map<String, String> fillInData;
    public void fillInData(){
        if(this.getFillInData()!=null && !this.getFillInData().isEmpty()) {
            if(this.getData()!=null){
                if(this.getData().getContext()!=null){
                    this.getData().getContext().setBody(this.getFillInData());
                    this.getData().getContext().setHeader(this.getFillInData());
                }
            }
        }
    }
}
