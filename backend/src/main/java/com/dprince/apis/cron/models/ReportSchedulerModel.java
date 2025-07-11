package com.dprince.apis.cron.models;

import com.dprince.apis.cron.models.enums.ScheduleTime;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class ReportSchedulerModel implements Institutionable {
    private Long institutionId;

    @NotNull(message = "Please specify the timing of the schedule")
    private ScheduleTime scheduleTime;

    @Max(value = 23, message = "The maximum value for hours is 23.")
    @PositiveOrZero(message = "The hour must be a positive number or Zero.")
    private int hours;

    @Max(value = 59, message = "The maximum value for minutes is 59.")
    @PositiveOrZero(message = "The minutes must be a positive number or Zero.")
    private int minutes;


    @JsonIgnore
    public String getCronExpression(){
        // second minute hour day-of-month month day-of-week
        switch(this.getScheduleTime()){
            case EVERY_DAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * *";

            case EVERY_MONDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * MON";

            case EVERY_TUESDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * TUE";

            case EVERY_WEDNESDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * WED";

            case EVERY_THURSDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * THU";

            case EVERY_FRIDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * FRI";

            case EVERY_SATURDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * SAT";

            case EVERY_SUNDAY:
                return "0 "+this.getMinutes()+" "+this.getHours()+" * * SUN";

            default:
            case EVERY_MONTH:
                return "0 "+this.getMinutes()+" "+this.getHours()+" 1 *";
        }
    }
}
