package com.testwa.distest.server.model.permission;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 2016/11/12.
 */
public enum UserShareScope {

    Self(0), User(1), Project(2), All(100);

    private int value;

    UserShareScope(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static boolean contains(String name){
        if(StringUtils.isBlank(name)){
            return false;
        }
        UserShareScope[] season = values();
        for(UserShareScope s : season){
            if(s.name().equals(name)){
                return true;
            }
        }

        return false;
    }

    public static UserShareScope fromValue(int value)
            throws IllegalArgumentException {
        try {
            return UserShareScope.values()[value];
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown enum value :"+ value);
        }
    }

    public static List<UserShareScope> lteScope(Integer scope) {
        List<UserShareScope> l = new ArrayList<>();
        switch (fromValue(scope)){
            case Self:
                if(Self.getValue() <= scope){
                    l.add(Self);
                }
                break;
            case User:
                if(User.getValue() <= scope){
                    l.add(User);
                }
                break;
            case Project:
                if(Project.getValue() <= scope){
                    l.add(Project);
                }
                break;
            case All:
                if(All.getValue() <= scope){
                    l.add(All);
                }
                break;
        }
        return l;
    }
}
