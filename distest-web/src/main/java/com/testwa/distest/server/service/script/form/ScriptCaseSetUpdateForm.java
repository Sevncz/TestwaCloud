package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;

import java.util.List;

@Data
public class ScriptCaseSetUpdateForm extends RequestFormBase {
    private Long id;
    private String caseName;
//    private String tag;
    private String description;
    private Long appInfoId;
    private String packageName;
    private String appName;
    private List<String> scriptCaseIds;
}
