package com.dprince.entities;


import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Table(name = "priest_signatures")
@Entity
public class PriestSignature implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    @NotNull(message = "The member must  be specified.")
    private Long memberId;

    @Column(nullable = false, length = Integer.MAX_VALUE)
    @NotBlank(message = "The image cannot be empty.")
    private String signatureImage;
}
