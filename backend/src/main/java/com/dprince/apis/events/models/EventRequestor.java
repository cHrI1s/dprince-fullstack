package com.dprince.apis.events.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class EventRequestor implements Institutionable {
    private Long institutionId;
    private Long eventId;
}
