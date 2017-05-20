package com.testwa.distest.server.util;

/**
 * Created by wen on 16/9/4.
 */
public enum DeviceType {

    ANDROID("Android"), IPHONE("iPhone"), OTHER("other");

    private String name;

    DeviceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
