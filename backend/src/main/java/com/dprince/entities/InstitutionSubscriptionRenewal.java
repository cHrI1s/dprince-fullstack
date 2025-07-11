package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.Subscription;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "institution_subscription_renewals",
        indexes = {
                @Index(name="index_institution_id", columnList = "institutionId"),
        })
public class InstitutionSubscriptionRenewal implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private Long subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription subscription;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date oldDeadline;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date newDeadline;
}
