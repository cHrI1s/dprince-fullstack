package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SingleInstitutionRequest implements Institutionable {
    @NotNull(message = "Institution not provided.")
    private Long institutionId;
}
