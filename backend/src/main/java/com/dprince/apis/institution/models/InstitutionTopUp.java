package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@FieldNameConstants
@Data
@Entity
@Table(name = "institution_top_ups",
        indexes={
                @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class InstitutionTopUp implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Please select an institution.")
    private Long institutionId;

    private Long sms;
    private Long emails;
    private Long whatsapp;
    private Long additionalUser;
    private Long backup;
}
