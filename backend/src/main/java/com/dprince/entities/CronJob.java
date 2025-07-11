package com.dprince.entities;

import com.dprince.apis.cron.models.enums.ScheduleTime;
import com.dprince.apis.utils.AppStringUtils;
import com.dprince.entities.enums.CronInitiator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cron_jobs",
        indexes = {
                @Index(name = "index_institution_id", columnList = "institutionId")
        })
public class CronJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ScheduleTime scheduleTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CronInitiator taskInitiator;

    @JsonIgnore
    @Column(nullable = false)
    private String taskId = AppStringUtils.generateRandomString(30);

    @Column(nullable = false)
    private int hours;

    @Column(nullable = false)
    private int minutes;

    @JsonIgnore
    @Column(nullable = false)
    private String cronExpression;

    @JsonIgnore
    @Column(nullable = false)
    private Long institutionId;
}

