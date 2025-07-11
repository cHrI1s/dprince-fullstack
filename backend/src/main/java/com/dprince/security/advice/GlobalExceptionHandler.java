package com.dprince.security.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

// @ControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(Exception.class)
    public ResponseStatusException handleGlobalException(Exception ex,
                                                         WebRequest request) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later");
    }
}
