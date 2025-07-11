package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.validations.ProspectusMemberValidation;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FamilyMemberModel {
    @NotNull(message = "Member must be provided.", groups = {
            ProspectusMemberValidation.class
    })
    private Long memberId;

    @NotNull(message = "Family relation must be provided.", groups = {
            ProspectusMemberValidation.class
    })
    private MemberOfFamilyTitle title;
}
