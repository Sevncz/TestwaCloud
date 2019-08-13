package com.testwa.distest.client.device.driver;

import lombok.Data;

@Data
public class TouchMultiPerformActionOption {
    private String element;
    private Integer x;
    private Integer y;
    private Integer count;
}
