
/*
 * Copyright (c) 2024. AlienBase
 * @author: Chris Ndayishimiye
 * Project: BlackVend
 */

package com.dprince.configuration.validation.annotations;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * * @author Chris Ndayishimiye
 * * @created 10/24/22
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=DatePastValidator.class)
public @interface DatePast {
    String message() default "";
    boolean presentAllowed() default false;
    boolean time() default false;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
