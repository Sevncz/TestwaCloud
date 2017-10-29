package com.testwa.core.entity;

public enum TypeEnum {
    Normal(0),  // 正常流程testcase
    Quick(1);   // 1键部署类型

    private int code;

    TypeEnum(int code) {
        this.code = code;
    }

    public static TypeEnum getEnumForValue(int value) {
        TypeEnum[] values = TypeEnum.values();
        for (TypeEnum eachValue : values) {
            if (eachValue.code == value) {
                return eachValue;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }
}

