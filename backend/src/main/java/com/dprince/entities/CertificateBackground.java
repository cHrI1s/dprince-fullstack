package com.dprince.entities;

import com.dprince.entities.enums.CertificateType;
import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@FieldNameConstants
@Data
@Entity
@Table(name = "certificates_backgrounds",
        indexes = {
                @Index(name="index_institution_id", columnList = "institutionId"),
                @Index(name="index_certificate_type", columnList = "certificateType"),
        })
public class CertificateBackground implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @NotNull(message = "Certificate Type should be defined!")
    @Column(nullable = false, length = 15)
    private CertificateType certificateType;

    @NotNull(message = "The name of the background image should be defined!")
    @Column(nullable = false)
    private String fileName;
}
