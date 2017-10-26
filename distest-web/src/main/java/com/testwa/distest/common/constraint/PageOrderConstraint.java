package com.testwa.distest.common.constraint;

import com.testwa.distest.common.constraint.validation.PageOrderValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * \
 *
 * @author liaoqiqi
 * @version 2014-1-14
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PageOrderValidator.class)
public @interface PageOrderConstraint {

    String message() default "page.order.error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
