package com.testwa.distest.server.util;

/**
 * Created by wen on 16/9/4.
 */
public enum ScriptType {

    PYTHON("python"), JAVA("java"), RUBY("ruby"), JAVASCRIPT("javascript"), OTHER("other");

    private String name;

    ScriptType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
