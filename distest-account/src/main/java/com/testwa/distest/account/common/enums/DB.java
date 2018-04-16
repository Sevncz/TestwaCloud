package com.testwa.distest.account.common.enums;


import com.fasterxml.jackson.annotation.JsonValue;
import com.testwa.core.base.enums.ValueEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DB {

    public enum Sex implements ValueEnum {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");
        private int value;
        private String desc;
        Sex(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        @JsonValue
        public String getDesc() {
            return desc;
        }
        public static Sex valueOf(int value) {
            Sex sex = UNKNOWN;
            switch (value) {
                case 1: sex = MALE;break;
                case 2: sex = FEMALE;break;
                default: sex = UNKNOWN;
            }
            return sex;
        }
    }

}