package com.testwa.core.base.util;

public abstract class Assert {

    public static void isNull(Object object, String message) {
        if(object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if(object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(String text, String message) {
        if (text != null && text.length() > 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(String text, String message) {
        if (text == null || text.length() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}