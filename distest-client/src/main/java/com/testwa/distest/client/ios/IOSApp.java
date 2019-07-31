package com.testwa.distest.client.ios;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class IOSApp {
    private String bundleId;
    private String appName;
    private String appVersion;
}
