package com.testwa.core.base.mybatis.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {

    /**
     * (Optional) The name of the table.
     * <p> Defaults to the entity name.
     *
     * @return name
     */
    String name() default "";

    /**
     * (Optional) The catalog of the table.
     * <p> Defaults to the default catalog.
     *
     * @return catalog
     */
    String catalog() default "";

    /**
     * (Optional) The schema of the table.
     * <p> Defaults to the default schema for user.
     *
     * @return schema
     */
    String schema() default "";
}