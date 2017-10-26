package com.testwa.distest.common.constraint;

import com.testwa.distest.common.constraint.validation.PasswordValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author liaoqiqi
 * @version 2014-1-14
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordConstraint {

    String message() default "password.not.right";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
