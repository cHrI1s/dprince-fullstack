package com.dprince.apis.events.models;


import com.dprince.apis.events.models.validators.ChurchEventCreationValidator;
import com.dprince.apis.events.models.validators.ChurchEventUpdateValidator;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;
import java.util.Date;

@Data
public class ChurchEventCreator {
    @NotBlank(message = "Please provide the event to update.",
            groups={
                ChurchEventUpdateValidator.class,
            })
    private Long eventId;

    private Long churchId;

    @NotBlank(message = "Please provide the title of the event.",
            groups={
                ChurchEventCreationValidator.class,
                ChurchEventUpdateValidator.class,
            })
    private String title;


    @NotBlank(message = "Please provide the description of the event.",
            groups={
                ChurchEventCreationValidator.class,
                ChurchEventUpdateValidator.class,
            })
    private String description;
    private String remarks;

    @Min(value = 1, message="At least one attendee must come to an event",
            groups={
                ChurchEventCreationValidator.class,
                ChurchEventUpdateValidator.class,
            })
    private int expectedAttendees;


    @Min(value = 1, message="At least one attendee must come to an event",
            groups={
                    ChurchEventCreationValidator.class,
                    ChurchEventUpdateValidator.class,
            })
    private int numberOfAttendees;

    @NotNull(message="The date of the event must be provided.",
            groups={
                ChurchEventCreationValidator.class,
                ChurchEventUpdateValidator.class,
            })
    @FutureOrPresent(message = "THe date of the event must be a date in the future.",
            groups={
                ChurchEventCreationValidator.class,
                ChurchEventUpdateValidator.class,
            })
    @Temporal(TemporalType.DATE)
    private Date eventDate;
}
