package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.CommunicationWay;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MemberSearchModel implements Institutionable {
    private Long institutionId;

    @NotBlank(message = "Enter something to search.")
    private String query;

    @NotNull(message = "Communication way unknown.")
    private CommunicationWay communicationWay;
}
