package com.testwa.distest.server.api;

import org.apache.commons.lang3.StringUtils;


/**
 * 定义 websocket 连接时 Flag 参数的内容
 * Created by wen on 17/12/2016.
 */
public enum WSFlagEnum {
    LOGCAT("logcat"), SCREEN("screen");

    private String value;

    WSFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static boolean contains(String name){
        if(StringUtils.isBlank(name)){
            return false;
        }
        WSFlagEnum[] season = values();
        for(WSFlagEnum s : season){
            if(s.getValue().equals(name)){
                return true;
            }
        }

        return false;
    }

}
