package com.testwa.distest.client.model;

import com.testwa.core.utils.NetUtil;
import lombok.Data;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by wen on 2016/10/16.
 */
@Data
public class AgentInfo {

    private String mac;
    private String host;
    private String osName;
    private String osVersion;
    private String osArch;
    private String javaVersion;

    private static class Holder {
        private static AgentInfo agentInfo = new AgentInfo();
    }

    private AgentInfo(){
        try {
            InetAddress ia = InetAddress.getLocalHost();
            this.host = NetUtil.getLocalIp(ia);
            this.mac = NetUtil.getLocalMac(ia);
        } catch (UnknownHostException e) {
            this.host = "unknow";
        } catch (SocketException e) {
            this.mac = "unknow";
        } catch (Exception e) {
            this.host = "unknow";
            this.mac = "unknow";
        }
        this.osName =  System.getProperty("os.name");
        this.osVersion =  System.getProperty("os.version");
        this.osArch =  System.getProperty("os.arch");
        this.javaVersion =  System.getProperty("java.version");

    }

    public static AgentInfo getAgentInfo() {
        return Holder.agentInfo;
    }

    public static void main(String[] args) throws Exception {
        AgentInfo ai = new AgentInfo();
        System.out.println(ai.toString());
    }

}
