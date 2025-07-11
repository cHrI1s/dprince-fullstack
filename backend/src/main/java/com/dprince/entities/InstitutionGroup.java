package com.dprince.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Table(name="institution_groups", indexes = {
        @Index(name="index_id" , columnList = "id")
})
@Data
@Entity
public class InstitutionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long institutionId;

    @NotBlank(message = "The group cannot be empty.")
    private String name;
}
