package com.testwa.distest.client.model;

import com.testwa.core.utils.NetUtil;
import lombok.Data;
import lombok.ToString;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by wen on 2016/10/16.
 */
@ToString
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
            InetAddress ia = getLocalHostLANAddress();
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

    private InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    public static AgentInfo getAgentInfo() {
        return Holder.agentInfo;
    }

    public static void main(String[] args) throws Exception {
        AgentInfo ai = new AgentInfo();
        System.out.println(ai.toString());
    }

}
