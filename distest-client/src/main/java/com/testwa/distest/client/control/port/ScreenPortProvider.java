package com.testwa.distest.client.control.port;

import com.testwa.core.PortProvider;

public class ScreenPortProvider{
    private static PortProvider portProvider;
	
	public static void init(int portStart,int portEnd){
		portProvider = new PortProvider(portStart,portEnd);
	}
	
	public static int pullPort(){
		return portProvider.pullPort();
	}
	
	public static boolean pushPort(int port){
		return portProvider.pushPort(port);
	}
}