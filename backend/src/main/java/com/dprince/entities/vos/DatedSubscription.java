package com.dprince.entities.vos;

import lombok.Data;

import java.util.Date;

@Data
public class DatedSubscription {
    private String name;
    private Date start;
    private Date deadline;
}
