
/*
 * Copyright (c) 2024. AlienBase
 * @author: Chris Ndayishimiye
 * Project: BlackVend
 */

package com.dprince.configuration.validation.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * * @author Chris Ndayishimiye
 * * @created 10/23/22
 */
@Data
public class ApiErrors {
    private HttpStatus status;
    private String message;
    private List<String> errors;

    public ApiErrors(HttpStatus httpStatus, String localizedMessage, List<String> errors){
        this.setStatus(httpStatus);
        this.setMessage(localizedMessage);
        this.setErrors(errors);
    }
}
