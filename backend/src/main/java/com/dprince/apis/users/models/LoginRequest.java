package com.dprince.apis.users.models;

import com.dprince.apis.users.validations.EmailOTPLogin;
import com.dprince.apis.users.validations.OTPLogin;
import com.dprince.apis.users.validations.OTPVerifier;
import com.dprince.apis.users.validations.UsernameLogin;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class LoginRequest {
    @NotBlank(message = "Username must not be empty.",
            groups={
                UsernameLogin.class,
                OTPVerifier.class
            })
    private String username;

    @Email(message = "Email is not valid.",
            groups = {EmailOTPLogin.class},
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    @NotBlank(message = "Email should be provided", groups = {EmailOTPLogin.class})
    private String email;


    @NotBlank(message = "Password must not be empty.",
            groups={
                UsernameLogin.class
            })
    private String password;

    @NotNull(message = "OTP should not be empty.",
            groups={
                OTPVerifier.class
            })
    @Min(value=100000, message = "OTP value must not be lesser than 100000", groups={OTPVerifier.class})
    @Max(value=999999, message = "OTP value must not be lesser than 999999", groups={OTPVerifier.class})
    private Integer otp;


    @NotNull(message = "Please provide the phone number",
            groups={
                OTPLogin.class
            })
    private Long phone;


    private boolean rememberUser;
}
