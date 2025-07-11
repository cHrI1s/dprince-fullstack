package com.dprince.apis.institution.models.parts;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.embeded.PersonAddress;
import com.dprince.entities.enums.AppLanguage;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.MemberStatus;
import com.dprince.entities.enums.Subscription;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProspectusMember extends PersonAddress implements Institutionable {
    private Long institutionId;

    @NotBlank(message = "First name must not be empty.")
    private String firstName;

    @NotBlank(message = "Last name must not be empty.")
    private String lastName;

    @NotNull(message = "Please specify the title.")
    private Long titleId;

    @NotNull(message = "Please specify the gender.")
    private Gender gender;

    @Past(message = "Date of birth must be a date in the past.")
    @NotNull(message = "Date of birth must be provided.")
    private Date dob;

    private Date dom;

    private String profile;
    private Boolean communion;
    private Date communionDate;

    // @NotBlank(message = "Email must be provided.")
    private String email;

    // Everyone
    @NotNull(message = "Language invalid.")
    private AppLanguage language;

    private Subscription subscription;

    @NotNull(message = "Member status Invalid.")
    private MemberStatus status;
}
