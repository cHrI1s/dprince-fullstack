package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.Date;

@FieldNameConstants
@Data
@Entity
@Table(name = "church_contributions",
        indexes = {
            @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class ChurchContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false, length = 60)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate = DateUtils.getNowDateTime();
}
