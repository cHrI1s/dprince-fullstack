package com.dprince.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the login_details collection
 *
 */
@Data
@Entity(name="login_details")
public class LoginDetail {
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private boolean isValid;

    private Date lastActivityTime;

    private boolean rememberMe;

    @Column(nullable = false, length = 500, unique = true)
    private String token;

    private Long userId;

    private String ipAddress;
    private Date createdDate;
}