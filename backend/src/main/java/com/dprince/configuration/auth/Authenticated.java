package com.dprince.configuration.auth;

import com.dprince.entities.enums.UserType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * * @author Chris Ndayishimiye
 * * @created 11/13/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface Authenticated {
    UserType[] userTypes() default {};
}
