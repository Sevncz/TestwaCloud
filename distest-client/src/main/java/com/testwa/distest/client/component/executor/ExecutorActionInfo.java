package com.testwa.distest.client.component.executor;import java.lang.annotation.*;@Retention(RetentionPolicy.RUNTIME)@Target({ElementType.FIELD, ElementType.METHOD})public @interface ExecutorActionInfo {    /**     * 动作描述     * @return     */    String desc();    int order();}