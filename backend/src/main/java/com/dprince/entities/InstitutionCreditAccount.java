package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "institution_credit_accounts",
        indexes={
                @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class InstitutionCreditAccount implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @NotBlank(message = "The name of the Credit account must not be null.")
    @Column(nullable = false, length = 30)
    private String name;
}
