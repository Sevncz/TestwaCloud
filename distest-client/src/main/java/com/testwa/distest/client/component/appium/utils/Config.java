package com.testwa.distest.client.component.appium.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Config {

    private static Environment evn;
    private static Config instance;

    private Config() {
    }

    private synchronized static Config getInstance() {
        if(evn == null){
            throw new RuntimeException("Environment is null");
        }
        if (null == instance) {
            instance = new Config();
        }
        return instance;
    }

    public static int getInt( String str){
        try {
            if (null == instance) {
                getInstance();
            }
            return Integer.parseInt(evn.getProperty( str ));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getLong( String str){
        try {
            if (null == instance) {
                getInstance();
            }
            return Long.parseLong( evn.getProperty( str ) );
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getDouble( String str){
        try {
            if (null == instance) {
                getInstance();
            }
            return Double.parseDouble(evn.getProperty( str ));

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getString( String str){
        try {
            if (null == instance) {
                getInstance();
            }
            return evn.getProperty( str );
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean getBoolean( String str){
        try {
            if (null == instance) {
                getInstance();
            }
            return Boolean.parseBoolean( evn.getProperty( str ));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getAgentId(){
        String agentId = getString("agent.id");
        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getLocalHost();
            if(StringUtils.isEmpty(agentId)){
                agentId = netAddress.getHostName();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
        return agentId;
    }

    public static void setEnv(Environment evn1){
        if(evn == null){
            evn = evn1;
        }else{
            throw new RuntimeException("Environment has exits");
        }
    }

}
