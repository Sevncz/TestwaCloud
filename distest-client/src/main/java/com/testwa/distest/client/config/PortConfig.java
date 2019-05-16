package com.testwa.distest.client.config;

/**
 * 端口配置文件
 */
public class PortConfig {
    /**
     * 触控端口映射开始号
     */
    public final static int touchPortStart=7000;
	public final static int touchPortEnd=7499;

    /**
     * 屏幕端口映射开始号
     */
	public final static int screenPortStart=7500;
	public final static int screenPortEnd=7999;

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
	public final static int wdaPortStart=9000;
	public final static int wdaPortEnd=9499;

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