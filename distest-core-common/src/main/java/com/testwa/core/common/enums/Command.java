package com.testwa.core.common.enums;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.security.InvalidParameterException;

/**
 * Created by wen on 10/06/2017.
 */
public class Command {

    public enum Schem {
        WAIT("wait"),
        OPEN("open"),
        START("start"),
        WAITTING("waitting"),
        TOUCH("touch"),
        DEVICES("devices"),
        KEYEVENT("keyevent"),
        INPUT("input"),
        SHOT("shot"),
        MINICAP("minicap"),
        MINITOUCH("minitouch"),
        STFAGENT("stfagent"),
        START_TASK("start_task"),
        COMPLETE_TASK("complete_task"),
        CANCEL_TASK("cancel_task"),
        START_LOGCAT("start_logcat"),
        WAIT_LOGCAT("wait_logcat"),
        INSTALL("install"),
        UNINSTALL("uninstall"),
        PUSH("push"),
        BACK("back"),
        HOME("home"),
        MENU("menu"),
        SHELL("shell"),
        OPENWEB("openweb"),
        MESSAGE("message");

        private String schemStr;

        public String getSchemString() {
            return schemStr;
        }

        Schem(String str) {
            schemStr = str;
        }
    }

    private Schem schem;
    private Object content;

    public Command(String event, String command) throws InvalidParameterException {
        // 截取schem
        switch (event) {
            case "wait":
                schem = Schem.WAIT;
                break;
            case "open":
                schem = Schem.OPEN;
                break;
            case "start":
                schem = Schem.START;
                break;
            case "waitting":
                schem = Schem.WAITTING;
                break;
            case "touch":
                schem = Schem.TOUCH;
                break;
            case "devices":
                schem = Schem.DEVICES;
                break;
            case "keyevent":
                schem = Schem.KEYEVENT;
                break;
            case "shot":
                schem = Schem.SHOT;
                break;
            case "input":
                schem = Schem.INPUT;
                break;
            case "minicap":
                schem = Schem.MINICAP;
                break;
            case "minitouch":
                schem = Schem.MINITOUCH;
                break;
            case "stfagent":
                schem = Schem.STFAGENT;
            case "start_task":
                schem = Schem.START_TASK;
                break;
            case "cancel_task":
                schem = Schem.CANCEL_TASK;
                break;
            case "start_logcat":
                schem = Schem.START_LOGCAT;
                break;
            case "wait_logcat":
                schem = Schem.WAIT_LOGCAT;
                break;
            case "install":
                schem = Schem.INSTALL;
                break;
            case "uninstall":
                schem = Schem.UNINSTALL;
                break;
            case "push":
                schem = Schem.PUSH;
                break;
            case "back":
                schem = Schem.BACK;
                break;
            case "home":
                schem = Schem.HOME;
                break;
            case "menu":
                schem = Schem.MENU;
                break;
            case "shell":
                schem = Schem.SHELL;
                break;
            case "openweb":
                schem = Schem.OPENWEB;
                break;
            case "message":
                schem = Schem.MESSAGE;
                break;
            default:
                throw new InvalidParameterException(command + " 未知的schem");
        }

        // 此消息不是json格式。其他都为json键值对
        if (!schem.equals(Schem.TOUCH) &&
                !schem.equals(Schem.KEYEVENT) &&
                !schem.equals(Schem.INPUT) &&
                !schem.equals(Schem.MINICAP) &&
                !schem.equals(Schem.MINITOUCH) &&
                !schem.equals(Schem.OPENWEB) &&
                !schem.equals(Schem.SHELL) &&
                !schem.equals(Schem.MESSAGE)) {
            try {
                this.content = parseContentJson(command);
            } catch (JSONException e) {
                throw new InvalidParameterException(e.getMessage());
            }
        } else {
            this.content = command;
        }
    }

    private Object parseContentJson(String content) throws JSONException {
        if (content == null || content.isEmpty()) {
            return JSONObject.parse("{}");
        }

        Object jsonObj = JSONObject.parse(content);
        if (jsonObj == null) {
            throw new JSONException(content + " 无法解析该json");
        }
        return jsonObj;
    }

    public Schem getSchem() {
        return schem;
    }

    public String getCommandString() {
        return schem.getSchemString() + "://" + getContent();
    }

    public String getContent() {
        if (content != null) {
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof JSONObject){
                return JSONObject.toJSONString(content);
            }
        }
        return "";
    }

    public String getString(String key, String defVal) {
        if (content != null && content instanceof  JSONObject) {
            String s = ((JSONObject) content).getString(key);
            return s == null ? defVal : s;
        }
        return "";
    }

    public Object get(String key) {
        if (content != null && content instanceof JSONObject) {
            return ((JSONObject) content).get(key);
        }
        return null;
    }

    public static Command ParseCommand(String event, String command) {
        try {
            Command cmd = new Command(event, command);
            return cmd;
        } catch (InvalidParameterException ex) {
            return null;
        }
    }
}
