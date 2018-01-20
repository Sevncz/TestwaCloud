package com.testwa.distest.server.service.testcase.form;

import com.testwa.core.base.form.RequestListBase;
import lombok.Data;

/**
 * Created by wen on 23/10/2017.
 */
@Data
public class TestcaseListForm extends RequestListBase {

    private Long projectId;
    private String caseName;

}
