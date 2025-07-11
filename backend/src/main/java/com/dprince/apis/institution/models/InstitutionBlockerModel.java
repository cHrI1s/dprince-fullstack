package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstitutionBlockerModel implements Institutionable {
    @NotNull(message = "Please specify the institution to block.")
    private Long institutionId;
}
