package com.testwa.distest.common.constraint;

import com.testwa.distest.common.constraint.validation.ListInValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * in 操作，目前仅支持 Integer，用逗号分隔
 *
 * @author liaoqiqi
 * @version 2014-1-26
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ListInValidator.class)
public @interface ListInConstraint {

    String message() default "in.list.error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return
     */
    String allowIntegerList() default "";
}
