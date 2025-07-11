package com.dprince.apis.users.models;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordRequest {
    @NotBlank(message = "Password must not be empty.")
    private String password;

    @NotBlank(message = "Confirm password must not be empty.")
    private String confirmPassword;
}
