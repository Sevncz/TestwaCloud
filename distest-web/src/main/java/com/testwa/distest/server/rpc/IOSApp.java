package com.testwa.distest.server.rpc;

import io.rpc.testwa.agent.AgentApp;
import io.rpc.testwa.agent.AppListEvent;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class IOSApp {
    private String bundleId;
    private String appName;
    private String appVersion;
    private Integer state;

    public IOSApp(AgentApp agentApp) {
        this.bundleId = agentApp.getBundleId();
        this.appName = agentApp.getAppName();
        this.appVersion = agentApp.getAppVersion();
        this.state = agentApp.getState();
    }
}
