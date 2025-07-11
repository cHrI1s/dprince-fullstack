package com.dprince.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;


@Data
@Entity
@Table(name="person_titles")
public class PersonTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title must be provided")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Title Short name must be provided")
    @Column(nullable = false)
    private String shortName;
}
