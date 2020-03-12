package com.testwa.distest.client.task;

import com.testwa.core.script.Function;
import lombok.Data;

import java.util.List;

@Data
public class FunctionCodeEntity {
    private Integer code;
    private List<Function> data;
    private Boolean success;
}
