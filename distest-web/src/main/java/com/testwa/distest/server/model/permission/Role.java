package com.testwa.distest.server.model.permission;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by wen on 2016/11/12.
 */
@Document(collection = "oauth2_role")
public class Role {

    @Id
    private String id;

    private String name;
    private Integer code;
    private String value;

    public Role() {
    }

    public Role(String name, Integer code, String value) {
        this.name = name;
        this.code = code;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
