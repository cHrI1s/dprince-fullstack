package com.dprince.configuration.interceptors;

import lombok.Data;

import java.io.Serializable;

@Data
public class BearerTokenWrapper implements Serializable {
    private static final long serialVersionUID = 1234567890L;
    private String token;
}
