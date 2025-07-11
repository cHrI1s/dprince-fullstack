package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.CommunicationWay;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name = "receipt_acknowledgements", indexes = {
        @Index(name = "idx_institution_id", columnList = "institutionId"),
        @Index(name = "idx_receipt_id", columnList = "receiptId")
})
public class ReceiptAcknowledgement {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @JsonIgnore
    @NotNull(message = "The receipt should not be empty.")
    private Long receiptId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationWay communicationWay = CommunicationWay.SMS;

    @Temporal(TemporalType.TIMESTAMP)
    private Date messageSendingTime = DateUtils.getNowDateTime();

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastReceiptPrint;

    @JsonIgnore
    private Long senderId;
}
