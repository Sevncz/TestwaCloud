package com.testwa.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@Data
public class RemoteRunCommand {

    private Long exeId;
    private Long appId;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private String install;
    private CommandEnum cmd;  // 0 关闭，1 启动

    public enum CommandEnum {
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

}
