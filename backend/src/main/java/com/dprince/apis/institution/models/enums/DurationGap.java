package com.dprince.apis.institution.models.enums;

import lombok.Getter;

@Getter
public enum DurationGap {
    YEAR("This Year"),
    LAST_SIX_MONTH("Last 6 Months"),
    LAST_THREE_MONTH("This 3 Months"),
    LAST_MONTH("Last Month"),
    LAST_SEVEN_DAYS("Last 7 Days"),
    TODAY("Today"),
    SINGLE_DATE("Single Date"),
    CUSTOM_DATE_GAP("Custom Date Gap"),
    LIFE_TIME("All Time");

    private final String displayName;
    DurationGap(String displayName){
        this.displayName = displayName;
    }
}
