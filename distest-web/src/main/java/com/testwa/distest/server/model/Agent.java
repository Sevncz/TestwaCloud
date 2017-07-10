package com.testwa.distest.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by wen on 2016/10/16.
 */
@Document(collection = "t_agent")
public class Agent {
    @Id
    private String id;

    private String host;

    private String agentKey;

    private String mac;

    private String osName;

    private String osVersion;

    private String osArch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAgentKey() {
        return agentKey;
    }

    public void setAgentKey(String agentKey) {
        this.agentKey = agentKey;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public void toEntity(com.testwa.distest.client.rpc.proto.Agent.SystemInfo agent) {
        this.agentKey = agent.getAgentKey();
        this.host = agent.getHost();
        this.mac = agent.getMac();
        this.osArch = agent.getOsArch();
        this.osName = agent.getOsName();
        this.osVersion = agent.getOsVersion();
    }
}
