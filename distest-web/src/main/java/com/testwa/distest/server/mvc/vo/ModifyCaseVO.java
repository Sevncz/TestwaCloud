package com.testwa.distest.server.mvc.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yxin on 8/8/2017.
 */
@Data
public class ModifyCaseVO extends CreateCaseVO {
    @NotNull
    public String id;
}
