package com.dprince.apis.users.models;

import com.dprince.entities.enums.CommunicationWay;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OtpResendRequest {
    private Long phone;
    private String email;

    @NotNull(message = "Type of OTP must be provided.")
    private CommunicationWay otpType;
}
