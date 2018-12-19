package com.testwa.core.base.mybatis.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Logic {

    EQ(" = ", "equal"),

    NOT_EQ(" != ", "not equal"),

    LT(" < ", "less than"),

    GT(" > ", "grater than"),

    LTEQ(" <= ", "less than or equal"),

    GTEQ(" >= ", "grater than or equal"),

    LIKE(" like ", "like"),

    NOT_LIKE(" not like ", "not like"),

    IN(" in ", "in"),

    NOT_IN(" not in ", "not in"),

    NULL(" is null ", "is null"),

    NOT_NULL(" is not null ", "is not null"),;


    private String code;

    private String desc;

}