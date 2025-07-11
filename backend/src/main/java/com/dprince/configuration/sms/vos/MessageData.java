package com.dprince.configuration.sms.vos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageData {
    @JsonProperty("Number")
    private String number;

    @JsonProperty("MessageId")
    private String messageId;

}
