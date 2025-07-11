
/*
 * Copyright (c) 2024. AlienBase
 * @author: Chris Ndayishimiye
 * Project: BlackVend
 */

package com.dprince.configuration.validation.annotations;

import com.dprince.apis.utils.DateUtils;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Calendar;
import java.util.Date;

/**
 * * @author Chris Ndayishimiye
 * * @created 10/23/22
 */
@Data
public class DateFutureValidator implements ConstraintValidator<DateFuture, Date> {
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean presentAllowed;

    private boolean dateTime;

    @Override
    public void initialize(DateFuture constraintAnnotation) {
        // ConstraintValidator.super.initialize(constraintAnnotation);
        this.setPresentAllowed(constraintAnnotation.presentAllowed());
        this.setDateTime(constraintAnnotation.time());
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        Date actualDate = DateUtils.getNowDateTime();
        if(!this.isDateTime()){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(actualDate);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            actualDate = calendar.getTime();
        }
        if(value==null) return false;
        return (this.isPresentAllowed())
                ? value.compareTo(actualDate)>=0
                : value.compareTo(actualDate)>0;
    }
}
