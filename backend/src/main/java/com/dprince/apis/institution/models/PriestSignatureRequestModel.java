package com.dprince.apis.institution.models;


import com.dprince.apis.institution.models.validations.DefaultSignatureSetter;
import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PriestSignatureRequestModel implements Institutionable {
    private Long institutionId;

    @NotNull(message = "The priest is not defined", groups = {DefaultSignatureSetter.class})
    private Long priestId;
}
