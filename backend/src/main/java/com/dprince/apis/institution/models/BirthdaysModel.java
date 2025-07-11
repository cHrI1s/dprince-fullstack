package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.enums.BDType;
import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BirthdaysModel implements Institutionable {
    private Long institutionId;

    @NotNull(message = "Please specify the type of birthday")
    private BDType birthdayType;

    @NotNull(message = "Please provide a specific date or starting date to use as filter")
    private Date start;

    private Date end;

    private int size = 5;
    private int page = 1;
}
