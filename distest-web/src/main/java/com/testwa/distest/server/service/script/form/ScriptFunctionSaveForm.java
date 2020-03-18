package com.testwa.distest.server.service.script.form;

import lombok.Data;

import java.util.List;

@Data
public class ScriptFunctionSaveForm {
    private String title;
    private String functionId;
    private String action;
    private List<Object> parameter;
    private Integer seq;
}
