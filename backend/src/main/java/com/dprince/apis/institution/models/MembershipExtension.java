package com.dprince.apis.institution.models;


import com.dprince.apis.institution.models.parts.MemberAction;
import com.dprince.entities.enums.Subscription;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


@EqualsAndHashCode(callSuper = true)
@Data
public class MembershipExtension extends MemberAction {
    @NotNull(message = "Membership extension duration not specified")
    private Subscription duration;
}
