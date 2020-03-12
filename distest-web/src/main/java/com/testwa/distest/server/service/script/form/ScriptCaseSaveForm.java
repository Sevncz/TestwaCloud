package com.testwa.distest.server.service.script.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;

import java.util.List;

@Data
public class ScriptCaseSaveForm extends RequestFormBase {
    private String scriptName;
    private String scriptDesc;
    private String platform;
    private String appBasePackage;
    private List<ScriptFunctionSaveForm> scriptFunctions;
}
