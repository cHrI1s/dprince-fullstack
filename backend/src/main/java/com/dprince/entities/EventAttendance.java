package com.dprince.entities;

import com.dprince.entities.utils.AppRepository;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "events_attendance",
        indexes = {
                @Index(name = "idx_institution_id", columnList = "institutionId"),
                @Index(name = "index_event", columnList = "eventId"),
        })
public class EventAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long takenBy;
}
