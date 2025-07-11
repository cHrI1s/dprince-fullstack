package com.dprince.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "pincodes")
@Entity
 public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String code;
    
}
