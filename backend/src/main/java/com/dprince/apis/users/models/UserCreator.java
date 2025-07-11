package com.dprince.apis.users.models;

import com.dprince.apis.users.models.validation.UserCreationValidation;
import com.dprince.apis.users.models.validation.UserUpdateValidation;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.UserType;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class UserCreator {
    private boolean passwordUpdate;
    @NotNull(message = "User to update not specified",
            groups = {
                UserUpdateValidation.class
            })
    private Long id;

    @NotNull(message = "Please specify the user type.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public UserType userType;

    @NotBlank(message="Staff Id must not be empty.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public String staffId;

    @NotBlank(message="First name must not be empty.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public String firstName;

    @NotBlank(message="Last name must not be empty.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public String lastName;

    @Email(message = "Email is not valid. Username must be a valid email.",
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    @NotBlank(message="Username must not be empty.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public String username;

    @NotBlank(message="Password must not be empty.",
            groups = {
                UserCreationValidation.class
            })
    public String password;

    @NotBlank(message="Confirm password must not be empty.",
            groups = {
                UserCreationValidation.class
            })
    public String confirmPassword;

    @Min(value = 0, message = "Phone number must be greater than 0.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    @NotNull(message="Phone must not be empty.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public Long phone;

    @NotNull(message="Gender must be provided.",
            groups = {
                UserCreationValidation.class,
                UserUpdateValidation.class
            })
    public Gender gender;
}
