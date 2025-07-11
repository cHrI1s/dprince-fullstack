
/*
 * Copyright (c) 2024. AlienBase
 * @author: Chris Ndayishimiye
 * Project: BlackVend
 */

package com.dprince.configuration.validation;

import com.dprince.configuration.validation.exception.ApiErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * * @author Chris Ndayishimiye
 * * @created 10/23/22
 * * It previously extended ResponseEntityExceptionHandler
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class NotValidException {
    @NonNull
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : exception.getBindingResult().getFieldErrors()) {
            try{
                if(error.getDefaultMessage()!=null) {
                    errors.add(error.getDefaultMessage());
                }
            } catch (Exception ignored) {
                errors.add(error.getDefaultMessage());
            }
        }
        for (final ObjectError error : exception.getBindingResult().getGlobalErrors()) {
            errors.add(error.getDefaultMessage());
        }
        final ApiErrors apiError = new ApiErrors(HttpStatus.BAD_REQUEST,
                "Method Arguments are not valid.", errors);

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
