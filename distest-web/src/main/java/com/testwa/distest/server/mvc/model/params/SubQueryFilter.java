package com.testwa.distest.server.mvc.model.params;

import java.util.Map;

public class SubQueryFilter{
    private String matchMode;
    private String name;
    private Object value;

    public SubQueryFilter(String matchMode, String name, Object value) {
        this.matchMode = matchMode;
        this.name = name;
        this.value = value;
    }

    public SubQueryFilter(Map<String, Object> filter) {
        if(filter != null){
            this.matchMode = (String) filter.getOrDefault("matchMode", "");
            this.name = (String) filter.getOrDefault("name", "");
            this.value = filter.getOrDefault("value", "");
        }
    }

    public String getMatchMode() {
            return matchMode;
        }

    public void setMatchMode(String matchMode) {
        this.matchMode = matchMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
            this.name = name;
        }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}