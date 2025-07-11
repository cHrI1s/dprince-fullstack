package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.validations.AdminCreatorValidator;
import com.dprince.apis.institution.models.validations.AdminMakerValidator;
import com.dprince.apis.institution.models.validations.AdminUpdateValidator;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.UserType;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AdminCreator implements Institutionable {
    @NotNull(message = "Member to make admin is not set.",
            groups={
                AdminMakerValidator.class
            })
    private Long memberId;

    @NotNull(message = "Admin not specified.",
            groups={
                AdminUpdateValidator.class
            })
    private Long id;
    private Long institutionId;

    @NotBlank(message = "Staff Id must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private String adminCode;

    @NotBlank(message = "First name must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private String firstName;

    @NotBlank(message = "Last name must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private String lastName;

    @NotNull(message = "Gender must be specified.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private Gender gender;

    @Email(message = "Email is not valid.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    @NotBlank(message = "Email must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private String email;

    @Min(value = 1, message = "THe phone number must be a valid positive number.")
    @NotNull(message = "Phone must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class
            })
    private Long phone;

    @NotBlank(message = "Username must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminUpdateValidator.class,
                AdminMakerValidator.class
            })
    private String username;

    @NotBlank(message = "Password must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminMakerValidator.class
            })
    private String password;

    @NotBlank(message = "Confirm Password must not be empty.",
            groups={
                AdminCreatorValidator.class,
                AdminMakerValidator.class
            })
    private String confirmPassword;

    @NotNull(message = "Please precise the type of user you are creating.",
            groups={
                    AdminCreatorValidator.class,
                    AdminMakerValidator.class
            })
    private UserType adminType;
}
