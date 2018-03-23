package com.testwa.core.cmd;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class MiniCmd {

    private String type;
    private Map<String, Object> config = new HashMap<>();

    public void setScale(Float scale){
        config.put("scale", scale);
    }
    public void setRotate(Float rotate){
        config.put("rotate", rotate);
    }
}
