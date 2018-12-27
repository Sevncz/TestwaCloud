package com.testwa.distest.server.service.apitest.form;

import com.testwa.core.base.form.RequestFormBase;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * 表单：新增一个 API
 *
 * @author wen
 * @create 2018-12-17 18:39
 */
@ApiModel(value = "CategoryNewForm",
        description = "创建一个ApiCategory"
)
@Data
public class CategoryNewForm extends RequestFormBase {
    @NotBlank
    @Length(min = 1, max = 500)
    private String name;
    private Map<String, String> authorization;
    private String preScript;
    private String script;
    @Length(min = 1, max = 10000)
    private String description;

}
