package com.dprince.apis.dashboard.utils;

import com.dprince.apis.dashboard.utils.enums.DashboardDuration;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.vos.DateGap;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class DashboardUtils {

    public static DateGap getDateGap(@NonNull DashboardDuration duration,
                                     @Nullable Date date){
        Date startingDate,
                endingDate = (date==null) ? DateUtils.getNowDateTime() : date;
        Calendar calendar = GregorianCalendar.getInstance();
        switch(duration){
            case WEEK:
            default:
                startingDate = DateUtils.atStartOfWeek(endingDate);
                endingDate = DateUtils.atEndOfWeek(endingDate);
                break;

            case FIFTEEN_DAYS:
                calendar.add(Calendar.DAY_OF_YEAR, -15);
                startingDate = calendar.getTime();
                break;

            case MONTH:
                startingDate = DateUtils.getFirstDateOfMonth(null);
                endingDate = DateUtils.getLastDayOfMonth(endingDate);
                break;

            case THREE_MONTH:
            case SEMESTER:
                calendar.add(Calendar.MONTH,
                        duration.equals(DashboardDuration.SEMESTER)
                                ? -6
                                : -3);
                startingDate = DateUtils.getFirstDateOfMonth(calendar.getTime());
                endingDate = DateUtils.getLastDayOfMonth(endingDate);
                break;

            case YEAR:
                startingDate = DateUtils.getFirstDateOfYear(endingDate);
                endingDate = DateUtils.getLastDateOfYear(endingDate);
                break;

            case LAST_YEAR:
                calendar.add(Calendar.YEAR, -1);
                startingDate = DateUtils.getFirstDateOfYear(calendar.getTime());
                endingDate = DateUtils.getLastDateOfYear(startingDate);
                break;
        }
        startingDate = DateUtils.atTheBeginningOfTheDay(startingDate);
        endingDate = DateUtils.atTheEndOfTheDay(endingDate);
        return DateGap.builder()
                .end(endingDate)
                .from(startingDate)
                .build();
    }

    public static DateGap getDateGap(@NonNull DashboardDuration duration){
        return getDateGap(duration, null);
    }
}
