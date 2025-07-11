package com.dprince.apis.group.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Data
public class GroupUpdateModel {
    @NotBlank(message = "The id cannot be empty.")
    private Long id;

    @NotBlank(message = "The group cannot be empty.")
    private Long institutionId;

    @NotBlank(message = "The group cannot be empty.")
    private String name;
}
