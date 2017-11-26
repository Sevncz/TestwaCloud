package com.testwa.core.base.constraint;

import com.testwa.core.base.constraint.validation.PageOrderValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


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
