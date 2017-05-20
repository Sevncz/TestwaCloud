package com.testwa.distest.server.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by wen on 16/9/4.
 */
public enum AppType {

    ANDROID("Android"), IOS("iOS"), OTHER("other");

    private String name;

    AppType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
