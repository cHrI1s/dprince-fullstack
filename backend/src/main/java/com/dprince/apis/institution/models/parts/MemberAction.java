package com.dprince.apis.institution.models.parts;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberAction implements Institutionable {
    private Long institutionId;

    @NotNull(message = "Please select the Member.")
    private Long memberId;
}
