package com.testwa.distest.server.service.testcase.form;

import com.testwa.core.base.form.RequestFormBase;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Data
public class TestcaseNewForm extends RequestFormBase {
    @NotBlank
    public String name;
    public String description;
    @NotEmpty
    public List<Long> scriptIds;
    private String tag;
    private Long appInfoId;

}
