package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.validations.ProspectusMemberValidation;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.capabilities.Internationalizable;
import com.dprince.entities.embeded.PersonAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class FamilyCreatorModel extends PersonAddress
        implements Institutionable, Internationalizable {
    private Long id;
    private Long institutionId;

    @NotNull(message = "Family head should be specified.", groups={
            ProspectusMemberValidation.class
    })
    private Long familyHead;


    private Date dob;

    @Valid
    @Size(min = 2, message = "At least 2 members should be present to form a family.", groups={
            ProspectusMemberValidation.class
    })
    private List<FamilyMemberModel> members;

    @NotEmpty(message = "We could not locate your device. Please make sure your device is properly set.")
    private String clientTimezone = "Asia/Calcutta";

    private String photo;
}
