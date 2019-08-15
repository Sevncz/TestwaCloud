package com.testwa.distest.client.device.driver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TouchPerformAction {
    private String action;
    private TouchPerformActionOption options;
}
