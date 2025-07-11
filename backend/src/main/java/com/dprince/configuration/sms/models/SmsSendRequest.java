package com.dprince.configuration.sms.models;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class SmsSendRequest {
    private Long institutionId;
    private List<String> numbers;
    private String number;
    public void setNumber(String number) {
        this.number = number;
        if(this.numbers==null) this.numbers = new ArrayList<>();
        this.numbers.add(number);
    }

    private String message;
    private boolean directSend;
}
