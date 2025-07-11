package com.dprince.apis.events.vos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventAttendee {
    private String firstName;
    private String lastName;
    private String code;
}
