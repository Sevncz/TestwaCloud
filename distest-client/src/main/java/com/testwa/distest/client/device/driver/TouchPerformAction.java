package com.testwa.distest.client.device.driver;

import lombok.Data;

@Data
public class TouchPerformAction {
    private String action;
    private TouchPerformActionOption options;
}
