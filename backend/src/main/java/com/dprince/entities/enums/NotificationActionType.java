package com.dprince.entities.enums;

import lombok.Getter;

@Getter
public enum NotificationActionType {
    READ("Info"),
    DELETE("Error"),
    UPDATE("Warning"),
    CREATE("Success");

    private final String label;
    NotificationActionType(String label){
        this.label = label;
    }
}
