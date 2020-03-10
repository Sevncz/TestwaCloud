package com.testwa.distest.client.device.driver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TouchPerformActionOption {
//    private String element = "";
    private Integer x;
    private Integer y;
    private Integer count;
    private Integer ms;
}
