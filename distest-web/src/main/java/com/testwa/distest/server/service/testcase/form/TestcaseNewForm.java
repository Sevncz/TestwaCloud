package com.testwa.distest.server.service.testcase.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Data
public class TestcaseNewForm extends RequestFormBase {

    @NotNull
    public String name;
    public String description;
    @NotNull
    public Long projectId;
    @NotNull
    public List<Long> scriptIds;
    @NotNull
    private String tag;

}
