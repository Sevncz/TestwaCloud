package com.testwa.distest.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by wen on 10/07/2017.
 */
@Document(collection = "t_privilege")
public class Privilege {

    @Id
    private String id;

    private String priCode;
    private String priName;
    private String priType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPriCode() {
        return priCode;
    }

    public void setPriCode(String priCode) {
        this.priCode = priCode;
    }

    public String getPriName() {
        return priName;
    }

    public void setPriName(String priName) {
        this.priName = priName;
    }

    public String getPriType() {
        return priType;
    }

    public void setPriType(String priType) {
        this.priType = priType;
    }
}
