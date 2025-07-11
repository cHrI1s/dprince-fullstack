package com.dprince.apis.events.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.CommunicationWay;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FinalizeEventModel implements Institutionable {

    @NotNull(message = "Event not specified.")
    private Long id;

    @NotNull(message = "Institution not specified.")
    private Long institutionId;

    @NotNull(message = "A Communication way must be specified.")
    private CommunicationWay communicationWay;
}
