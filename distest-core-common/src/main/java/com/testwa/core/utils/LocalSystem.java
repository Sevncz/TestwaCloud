package com.testwa.core.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class LocalSystem {
    private static final Logger log = Logger.getLogger(LocalSystem.class.getName());

    public static InetAddress getInetAddress(){
        try{
            return InetAddress.getLocalHost();
        }catch(UnknownHostException e){
            log.severe("UnknownHostException");
        }
        return null;

    }

    public static String getLocalMac(InetAddress ia) throws SocketException {
        /**
         * 获取网卡，获取地址
         * 物理地址是48位，别和ipv6搞错了
         */
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<mac.length; i++) {
			if(i!=0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i]&0xff;
			String str = Integer.toHexString(temp);
			if(str.length()==1) {
				sb.append("0").append(str);
			}else {
				sb.append(str);
			}
		}
		return sb.toString().toUpperCase();
	}

    public static String getHostName(InetAddress ia){
        if(null == ia){
            return null;
        }
        String name = ia.getHostName();
        return name;
    }
}