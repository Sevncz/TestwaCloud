package com.testwa.core.cmd;

import lombok.Data;
import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
public class RemoteTestcaseContent {
    private Long testcaseId;
    private List<ScriptInfo> scripts;

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public List<ScriptInfo> getScripts() {
        return scripts;
    }

    public void setScripts(List<ScriptInfo> scripts) {
        this.scripts = scripts;
    }
}
