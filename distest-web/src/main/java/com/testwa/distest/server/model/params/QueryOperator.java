package com.testwa.distest.server.model.params;

/**
 * Created by wen on 2016/11/26.
 */
public enum QueryOperator {

    contains(0, "contains"), in(1, "in"), is(2, "is"), startwith(3, "startwith"), endwith(4, "endwith");

    private String name;
    private int value;

    QueryOperator(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
