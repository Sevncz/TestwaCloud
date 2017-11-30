package com.testwa.distest.device.enums;


import com.testwa.core.base.enums.ValueEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DB {

    public enum PhoneOS implements ValueEnum {
        UNKNOWN(0, "未知"),
        IOS(1, "iOS"),
        ANDROID(2, "Android"),
        WP(3, "WinPhone");
        private int value;
        private String desc;
        PhoneOS(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static PhoneOS valueOf(int value) {
            PhoneOS os = UNKNOWN;
            switch (value) {
                case 1: os = IOS;break;
                case 2: os = ANDROID;break;
                case 3: os = WP;break;
                default: os = UNKNOWN;
            }
            return os;
        }
    }

    public enum PhoneOnlineStatus implements ValueEnum {
        UNKNOWN(0, "未知"),
        ONLINE(1, "在线"),
        OFFLINE(2, "离线"),
        DISCONNECT(3, "断开");
        private int value;
        private String desc;
        PhoneOnlineStatus(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static PhoneOnlineStatus valueOf(int value) {
            PhoneOnlineStatus os = UNKNOWN;
            switch (value) {
                case 1: os = ONLINE;break;
                case 2: os = OFFLINE;break;
                case 3: os = DISCONNECT;break;
                default: os = UNKNOWN;
            }
            return os;
        }
    }
}