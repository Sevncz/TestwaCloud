package com.testwa.core.utils;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络工具类
 */
public class NetUtil {
    final static Logger logger = LoggerFactory.getLogger(NetUtil.class);

	private static void bindPort(String host, int port) throws IOException {
		Socket s = new Socket();
		s.bind(new InetSocketAddress(host, port));
		s.close();
	}

	/**
	 * 检测本地端口是否可用
	 * 
	 * @param port
	 *            端口号
	 * @return 如果可用返回true,不可用返回false
	 */
	public static boolean isPortAvailable(int port) {
		try {
			bindPort("127.0.0.1", port);
			bindPort("0.0.0.0", port);
			return true;
		} catch (IOException e) {
			logger.error("检测端口:" + port + " 被占用");
		}
		return false;
	}

	/**
	 * 获取本机内网ip
	 * 
	 * @return
	 */
	public static String getLocalIp(InetAddress ia) {
		return ia.getHostAddress();
	}

	public static String[] getAllLocalHostIP() {
        String[] ret = null;
		try {
            InetAddress ia = InetAddress.getLocalHost();
			String hostName = getLocalIp(ia);
			if (hostName.length() > 0) {
				InetAddress[] addrs = InetAddress.getAllByName("localhost");
				if (addrs.length > 0) {
					ret = new String[addrs.length];
					for (int i = 0; i < addrs.length; i++) {
						ret[i] = addrs[i].getHostAddress();
					}
				}
			}

		} catch (Exception ex) {
			ret = null;
		}
		return ret;
	}

	public static String getLocalMac(InetAddress ia) throws SocketException {
		//获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		StringBuffer sb = new StringBuffer();
		if(mac != null){
			for(int i=0; i<mac.length; i++) {
				if(i!=0) {
					sb.append("-");
				}
				//字节转换为整数
				int temp = mac[i]&0xff;
				String str = Integer.toHexString(temp);
				if(str.length()==1) {
					sb.append("0"+str);
				}else {
					sb.append(str);
				}
			}
		}
		return sb.toString().toUpperCase();
	}

	public static void main(String[] args) throws Exception {
        InetAddress ia = InetAddress.getLocalHost();
		System.out.println(getLocalMac(ia));
	}
}