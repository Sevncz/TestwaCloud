package com.testwa.distest.server.util;

import java.io.File;
import java.security.MessageDigest;

/**
 * Created by wen on 16/6/18.
 */
public class PathUtil {

    public static String getAppPath(String appPath, String projectName, String appName, String uniqstr, String alisname) {
        String apppath = buildNewAppPath(appPath, projectName, appName, uniqstr) + File.separator + alisname;

        File app = new File(apppath);
        if (!app.exists()) {
            apppath = appPath + File.separator + projectName +  File.separator + alisname;
        }

        return apppath;
    }

    public static String buildNewAppPath(String uploadpath, String projectName, String appName, String uniqstr){


        String md5 = MD5(projectName + appName);

        StringBuffer sb = new StringBuffer();
        sb.append(uploadpath).append(File.separator).append(md5).append(File.separator).append(uniqstr);
        return sb.toString();
    }

    public static String buildNewScriptPath(String uploadpath, String projectName, String scName) {

        String md5 = MD5(projectName + scName);
        StringBuffer sb = new StringBuffer();
        sb.append(uploadpath).append(File.separator).append(md5);
        return sb.toString();

    }


    public static String getScriptPath(String scriptPath, String projectName, String scName, String alisname) {
        String apppath = buildNewScriptPath(scriptPath, projectName, scName) + File.separator + alisname;

        File app = new File(apppath);
        if (!app.exists()) {
            apppath = scriptPath + File.separator + projectName +  File.separator + alisname;
        }

        return apppath;
    }

    public static String MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }
}
