package com.dprince.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Table(name = "pincodes")
@Entity
 public class PinCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "The pincode must be provided.")
    private Integer code;

    @NotBlank(message = "The District must be provided.")
    private String district;

    @NotBlank(message = "The State must be provided.")
    private String state;

    @NotBlank(message = "The Country must be provided.")
    private String country;
    
}
