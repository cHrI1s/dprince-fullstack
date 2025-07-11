package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@FieldNameConstants
@Data
@Entity
@Table(name = "guest_priests",
        indexes = {
            @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class GuestPriest implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @NotBlank(message = "First name should not be blank.")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name should not be blank.")
    @Column(nullable = false, length = 50)
    private String lastName;

    private Long titleId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fullName;
    public String getFullName(){
        return this.getFirstName().trim() +" "+this.getLastName().trim();
    }
}
