package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.ChurchFunction;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Data
public class PastorCreatorModel implements Institutionable {
    @NotNull(message = "The member must be specified.")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private ChurchFunction churchFunction;

    @NotNull(message = "The institution is not specified.")
    private Long institutionId;


}
