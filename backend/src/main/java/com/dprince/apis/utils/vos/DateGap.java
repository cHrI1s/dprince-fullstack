package com.dprince.apis.utils.vos;

import com.dprince.apis.utils.DateUtils;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class DateGap {
    private Date from;
    private Date end;

    public static DateGap getSingleDayDateDap(Date date){
        return DateGap.builder()
                .from(DateUtils.atTheBeginningOfTheDay(date))
                .end(DateUtils.atTheEndOfTheDay(date))
                .build();
    }
}
