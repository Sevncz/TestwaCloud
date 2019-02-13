package com.testwa.core.base.mybatis.annotation;

import com.testwa.core.base.mybatis.builder.Logic;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Condition {

    String property() default "";

    Logic logic() default Logic.EQ;

}