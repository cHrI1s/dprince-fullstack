package com.dprince.configuration.sms.vos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SmsDataOutput {
    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("MobileNumber")
    private String mobileNumber;

    @JsonProperty("JobId")
    private String jobId;

    @JsonProperty("MessageData")
    private List<MessageData> messageData;
}
