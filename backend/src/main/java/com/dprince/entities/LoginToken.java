package com.dprince.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * * @author Chris Ndayishimiye
 * * @created 6/17/2021
 */
@Data
@FieldNameConstants
@NoArgsConstructor
@Entity(name = "login_tokens")
public class LoginToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 500, unique = true)
    private String jwtToken;

    public LoginToken(String token){
        this.setJwtToken(token);
    }
}
