package com.testwa.distest.server.service.testcase.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by wen on 23/10/2017.
 */
@Data
public class TestcaseUpdateForm extends TestcaseNewForm {
    
    @NotEmpty
    public Long testcaseId;

}
