package com.dprince.apis.utils;

import com.dprince.entities.enums.Subscription;
import org.springframework.lang.NonNull;

import java.util.Calendar;
import java.util.Date;

public interface Subscriptionable {
    Date getDeadline();
    void setDeadline(Date deadline);

    Date getStart();

    default void computeDeadline(@NonNull Subscription subscription){
        Date deadline = this.getStart();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(deadline);
        switch (subscription){
            case ANNUAL:
                calendar.add(Calendar.YEAR, 1);
                break;

            case SEMESTRAL:
                calendar.add(Calendar.MONTH, 6);
                break;

            case QUARTER:
                calendar.add(Calendar.MONTH, 3);
                break;

            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;

            case WEEKLY:
                calendar.add(Calendar.DAY_OF_WEEK, 7);
                break;
        }
        this.setDeadline(calendar.getTime());
    }
}
