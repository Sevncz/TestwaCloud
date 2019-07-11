package com.testwa.distest.client.component.port;

import com.testwa.core.tools.PortProvider;

/**
 * socat 需要转发的端口
 */
public class SocatPortProvider {
    private static PortProvider portProvider;
	
	public static void init(int portStart,int portEnd){
		portProvider = new PortProvider(portStart,portEnd);
	}
	
	public synchronized static int pullPort(){
		return portProvider.pullPort();
	}
	
	public synchronized static boolean pushPort(int port){
		return portProvider.pushPort(port);
	}
}