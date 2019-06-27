package com.testwa.distest.client.config;

/**
 * 端口配置文件
 */
public class PortConfig {
    /**
     * 手机端端口映射开始号
     */
	public final static int apkPortStart=8000;
	public final static int apkPortEnd=8499;

    /**
     * appium端口映射开始号
     */
	public final static int appiumPortStart=8500;
	public final static int appiumPortEnd=8999;

    /**
     * appium端口映射开始号
     */
	public final static int iproxyPortStart =9000;
	public final static int iproxyPortEnd =9499;

    /**
     * android tcpip 命令端口
     */
	public final static int tcpipStart=9500;
	public final static int tcpipEnd=9999;

    /**
     * socat 转发命令端口
     */
	public final static int socatStart=10000;
	public final static int socatEnd=10499;
	
}