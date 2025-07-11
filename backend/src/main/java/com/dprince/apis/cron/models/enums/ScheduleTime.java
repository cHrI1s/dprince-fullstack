package com.dprince.apis.cron.models.enums;

import lombok.Getter;

@Getter
public enum ScheduleTime {
    EVERY_DAY("Every DAY"),
    EVERY_MONDAY("Every MONDAY"),
    EVERY_TUESDAY("Every TUESDAY"),
    EVERY_WEDNESDAY("Every WEDNESDAY"),
    EVERY_THURSDAY("Every THURSDAY"),
    EVERY_FRIDAY("Every FRIDAY"),
    EVERY_SATURDAY("Every SATURDAY"),
    EVERY_SUNDAY("Every SUNDAY"),
    EVERY_MONTH("Every MONTH");

    private final String displayName;
    ScheduleTime(String displayName){
        this.displayName = displayName;
    }
}
