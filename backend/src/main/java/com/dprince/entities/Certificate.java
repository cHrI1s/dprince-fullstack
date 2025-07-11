package com.dprince.entities;

import com.dprince.apis.certificates.validators.CertificateGeneration;
import com.dprince.apis.certificates.validators.CertificateRetrieval;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.behaviors.UserActionTraceable;
import com.dprince.entities.enums.CertificateType;
import com.dprince.entities.models.Certifier;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@FieldNameConstants
@Data
@Entity
@Table(name = "certificates",
        indexes = {
                @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class Certificate implements Institutionable, UserActionTraceable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @NotNull(message = "The owner of the certificate is not defined",
            groups = {
                CertificateGeneration.class,
                CertificateRetrieval.class
            })
    @Column(nullable = false)
    private Long ownerId;

    @NotNull(message = "Certificate type is not defined.",
            groups = {
                CertificateGeneration.class,
                CertificateRetrieval.class
            })
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date generationDate = DateUtils.getNowDateTime();

    @Column(nullable = false)
    private String number;
    public void generateNumber(@NonNull Certifier certifier,
                               @NonNull AppRepository appRepository){
        Long certificatesCount = appRepository.getCertificatesRepository()
                .countAllByCertificateTypeAndOwnerId(this.getCertificateType(), this.getOwnerId());
        if(certificatesCount==null) certificatesCount = 0L;

        // Activate the following line nibashaka ko numero izoba yayandi nyene
        long nextNumber = (certificatesCount==0) ? (certificatesCount + 1) : certificatesCount;
        // long nextNumber = certificatesCount + 1;
        if(certifier.getInstitution()==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Can not generate Certificate number!");
        }
        List<String> parts = new ArrayList<>();
        parts.add(certifier.getInstitution().getBaseCode().toUpperCase());
        parts.add(this.getCertificateType().getCode());
        parts.add(certifier.getOwnerCode());
        parts.add(nextNumber+"");
        this.setNumber(String.join("/", parts));
    }

    @JsonIgnore
    private Long doneBy;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String doer;
}
