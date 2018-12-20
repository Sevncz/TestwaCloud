package com.testwa.core.base.enums;

import java.util.Arrays;
import java.util.Optional;

public interface ValueEnum {
    int getValue();
    String getDesc();

    static <E extends Enum<?> & ValueEnum> Optional<E> valueOf(Class<E> enumClass, int value) {
        return Arrays.stream(enumClass.getEnumConstants()).filter(e -> e.getValue() == value).findAny();
    }

}