package com.testwa.distest.client.device.driver;

import lombok.Data;

@Data
public class TouchMultiPerformAction {
    private String action;
    private TouchMultiPerformActionOption options;
}
