package com.testwa.distest.client.model;

import com.testwa.core.utils.LocalSystem;
import com.testwa.distest.client.rpc.proto.Agent;

import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by wen on 2016/10/16.
 */
public class AgentSystem {

    private String agentKey;

    private String mac;

    private String host;

    private String osName;

    private String osVersion;

    private String osArch;

    public AgentSystem() {
        InetAddress ia = LocalSystem.getInetAddress();
        try {
            this.mac = LocalSystem.getLocalMac(ia);
            this.host = LocalSystem.getHostName(ia);
            this.osName = System.getProperty("os.name");
            this.osVersion = System.getProperty("os.version");
            this.osArch = System.getProperty("os.arch");
        } catch (SocketException e) {
            e.printStackTrace();
        }
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public Agent.SystemInfo toAgentSystemInfo(String agentKey) {
        return Agent.SystemInfo.newBuilder()
                .setAgentKey(agentKey)
                .setHost(this.getHost())
                .setMac(this.getMac())
                .setOsArch(this.getOsArch())
                .setOsName(this.getOsName())
                .setOsVersion(this.getOsVersion())
                .build();
    }
}
