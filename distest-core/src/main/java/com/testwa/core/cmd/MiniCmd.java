package com.testwa.core.cmd;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class MiniCmd {

    private String type;
    private Map<String, Object> config;

}
