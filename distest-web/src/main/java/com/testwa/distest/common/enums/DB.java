package com.testwa.distest.common.enums;


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
        @JsonValue
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
        @JsonValue
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


    public enum DeviceWorkStatus implements ValueEnum {
        UNKNOWN(0, "未知"),
        FREE(1, "空闲"),
        BUSY(2, "工作中");
        private int value;
        private String desc;
        DeviceWorkStatus(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        @JsonValue
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static DeviceWorkStatus valueOf(int value) {
            DeviceWorkStatus os = UNKNOWN;
            switch (value) {
                case 1: os = BUSY;break;
                case 2: os = FREE;break;
                default: os = UNKNOWN;
            }
            return os;
        }
    }

    public enum DeviceDebugStatus implements ValueEnum {
        UNKNOWN(0, "未知"),
        FREE(1, "空闲"),
        DEBUGGING(2, "调试中"),     // 观看界面 + 调试
        TRACKING(3, "远程观看界面");  // 观看界面
        private int value;
        private String desc;
        DeviceDebugStatus(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        @JsonValue
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static DeviceDebugStatus valueOf(int value) {
            DeviceDebugStatus os = UNKNOWN;
            switch (value) {
                case 1: os = FREE;break;
                case 2: os = DEBUGGING;break;
                case 3: os = TRACKING;break;
                default: os = UNKNOWN;
            }
            return os;
        }
    }


    public enum TaskStatus implements ValueEnum{
        NOT_EXECUTE(0, "未执行"),
        RUNNING(1, "正在运行"),
        COMPLETE(2, "已完成"),
        CANCEL(3, "取消"),
        ERROR(4, "异常"),
        TIMEOUT(5, "超时");
        private int value;
        private String desc;
        TaskStatus(int value, String desc){
            this.value = value;
            this.desc = desc;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static TaskStatus valueOf(int value) {
            TaskStatus se = ERROR;
            switch (value) {
                case 0: se = NOT_EXECUTE;break;
                case 1: se = RUNNING;break;
                case 2: se = COMPLETE;break;
                case 3: se = CANCEL;break;
                case 4: se = ERROR;break;
                case 5: se = TIMEOUT;break;
                default: se = ERROR;
            }
            return se;
        }

        public static List<Integer> notFinishedCode = new ArrayList<>(Arrays.asList(NOT_EXECUTE.getValue(), RUNNING.getValue()));
        public static List<Integer> finishedCode = new ArrayList<>(Arrays.asList(COMPLETE.getValue(), CANCEL.getValue(), ERROR.getValue()));

    }


    public enum ProjectRole implements ValueEnum {
        OWNER(0, "创建者"),
        ADMIN(1, "管理者"),
        MEMBER(2, "成员");
        private int value;
        private String desc;
        ProjectRole(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        @JsonValue
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static ProjectRole valueOf(int value) {
            ProjectRole role = OWNER;
            switch (value) {
                case 0: role = OWNER;break;
                case 1: role = ADMIN;break;
                case 2: role = MEMBER;break;
            }
            return role;
        }
    }


    public enum ScriptLN implements ValueEnum {
        UNKNOWN(0, "未知"),
        JAVA(1, "Java"),
        PYTHON(2, "Python"),
        JS(3, "js"),
        GO(4, "go"),
        PHP(5, "php"),
        RUBY(6, "ruby");
        private int value;
        private String desc;
        ScriptLN(int value, String desc){
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
        public static ScriptLN valueOf(Integer value) {
            if(value == null){
                return null;
            }
            ScriptLN ln = UNKNOWN;
            switch (value) {
                case 1: ln = JAVA;break;
                case 2: ln = PYTHON;break;
                case 3: ln = JS;break;
                case 4: ln = GO;break;
                case 5: ln = PHP;break;
                case 6: ln = RUBY;break;
                default: ln = UNKNOWN;
            }
            return ln;
        }
    }

    public enum TaskType implements ValueEnum {
        FUNCTIONAL(1, "回归测试"),
        COMPATIBILITY(2, "兼容测试"),
        PRESSURE(3, "压力测试"),
        CRAWLER(4, "遍历测试");

        private int value;
        private String desc;

        TaskType(int value, String desc){
            this.value = value;
            this.desc = desc;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static TaskType valueOf(int value) {
            TaskType tt = FUNCTIONAL;
            switch (value) {
                case 1: tt = FUNCTIONAL;break;
                case 2: tt = COMPATIBILITY;break;
                case 3: tt = PRESSURE;break;
                case 4: tt = CRAWLER;break;
                default: tt = FUNCTIONAL;
            }
            return tt;
        }
    }

    public enum DeviceLogType implements ValueEnum {
        DEBUG(0, "设备调试"),
        LOGCAT(1, "设备调试"),
        HG(11, "回归测试"),
        JR(12, "兼容测试"),
        YL(13, "压力测试"),
        CRAWLER(14, "遍历测试");

        private int value;
        private String desc;

        DeviceLogType(int value, String desc){
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
        public static DeviceLogType valueOf(int value) {
            DeviceLogType tt = DEBUG;
            switch (value) {
                case 0: tt = DEBUG;break;
                case 1: tt = HG;break;
                case 2: tt = JR;break;
                case 3: tt = YL;break;
                case 4: tt = CRAWLER;break;
                default: tt = DEBUG;
            }
            return tt;
        }
    }


    public enum CommandEnum implements ValueEnum  {
        STOP(0, "关闭"),
        START(1, "启动");
        private int value;
        private String desc;
        CommandEnum(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static CommandEnum valueOf(int value) {
            CommandEnum cmd = STOP;
            switch (value) {
                case 0: cmd = STOP;break;
                case 1: cmd = START;break;
                default: cmd = STOP;
            }
            return cmd;
        }
    }



    public enum DeviceShareScopeEnum implements ValueEnum {
        Private(0, "私有的"), Protected(1, "部分公开的"), Public(2, "公开的");

        private int value;
        private String desc;

        DeviceShareScopeEnum(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public static boolean contains(String name){
            if(StringUtils.isBlank(name)){
                return false;
            }
            DeviceShareScopeEnum[] season = values();
            for(DeviceShareScopeEnum s : season){
                if(s.name().equals(name)){
                    return true;
                }
            }

            return false;
        }

        public static DeviceShareScopeEnum fromValue(int value)
                throws IllegalArgumentException {
            try {
                return DeviceShareScopeEnum.values()[value];
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Unknown enum value :"+ value);
            }
        }
    }

    public enum DeviceShareScopeTypeEnum implements ValueEnum {

        User(0, "CurrentUser"), Project(1, "Project");

        private int value;
        private String desc;

        DeviceShareScopeTypeEnum(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public static boolean contains(String name){
            if(StringUtils.isBlank(name)){
                return false;
            }
            DeviceShareScopeTypeEnum[] season = values();
            for(DeviceShareScopeTypeEnum s : season){
                if(s.name().equals(name)){
                    return true;
                }
            }

            return false;
        }

        public static DeviceShareScopeTypeEnum fromValue(int value)
                throws IllegalArgumentException {
            try {
                return DeviceShareScopeTypeEnum.values()[value];
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Unknown enum value :"+ value);
            }
        }
    }

    public enum IssueStateEnum implements ValueEnum  {
        OPEN(0, "open", "开启"),
        FIXED(1, "fixed", " 已解决"),
        CLOSED(2, "closed", "已关闭"),
        REJECT(3, "rejected", "已拒绝");

        private int value;
        private String name;
        private String desc;

        IssueStateEnum(int value, String name, String desc){
            this.value = value;
            this.name = name;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }
        @JsonValue
        public String getName() {
            return name;
        }
        public String getDesc() {
            return desc;
        }

        public static IssueStateEnum valueOf(int value) {
            IssueStateEnum status = null;
            switch (value) {
                case 0: status = OPEN;break;
                case 1: status = CLOSED;break;
                case 2: status = REJECT;break;
            }
            return status;
        }

        public static IssueStateEnum nameOf(String name) {
            IssueStateEnum status = null;
            switch (name) {
                case "open": status = OPEN;break;
                case "closed": status = CLOSED;break;
                case "rejected": status = REJECT;break;
            }
            return status;
        }
    }



    public enum IssueOpTypeEnum implements ValueEnum  {
        ADD(0, "add", " 添加"),
        UPDATE(1, "update", "修改"),
        REMOVE(2, "remove", "移除"),
        CREATE(3, "create", "创建");

        private int value;
        private String name;
        private String desc;

        IssueOpTypeEnum(int value, String name, String desc){
            this.value = value;
            this.name = name;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }
        @JsonValue
        public String getName() {
            return name;
        }
        public String getDesc() {
            return desc;
        }

        public static IssueOpTypeEnum valueOf(int value) {
            IssueOpTypeEnum type = null;
            switch (value) {
                case 0: type = ADD;break;
                case 1: type = UPDATE;break;
                case 2: type = REMOVE;break;
                case 3: type = CREATE;break;
            }
            return type;
        }

        public static IssueOpTypeEnum nameOf(String name) {
            IssueOpTypeEnum status = null;
            switch (name) {
                case "add": status = ADD;break;
                case "update": status = UPDATE;break;
                case "remove": status = REMOVE;break;
                case "create": status = CREATE;break;
            }
            return status;
        }
    }

    public enum IssuePriorityEnum implements ValueEnum  {
        LOW(0, "低"),
        MIDDLE(1, "中"),
        HIGH(2, "高");
        private int value;
        private String desc;
        IssuePriorityEnum(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static IssuePriorityEnum valueOf(int value) {
            IssuePriorityEnum priorityEnum = LOW;
            switch (value) {
                case 0: priorityEnum = LOW;break;
                case 1: priorityEnum = MIDDLE;break;
                case 2: priorityEnum = HIGH;break;
                default: priorityEnum = LOW;
            }
            return priorityEnum;
        }
    }


    public enum IssueAssignRoleEnum implements ValueEnum  {
        LEADER(0, "负责人"),
        MEMBER(1, "协作人");
        private int value;
        private String desc;
        IssueAssignRoleEnum(int value, String desc){
            this.value = value;
            this.desc = desc;
        }
        public int getValue() {
            return value;
        }
        public String getDesc() {
            return desc;
        }
        public static IssueAssignRoleEnum valueOf(int value) {
            IssueAssignRoleEnum issueAssignRoleEnum = LEADER;
            switch (value) {
                case 0: issueAssignRoleEnum = LEADER;break;
                case 1: issueAssignRoleEnum = MEMBER;break;
                default: issueAssignRoleEnum = LEADER;
            }
            return issueAssignRoleEnum;
        }
    }


}