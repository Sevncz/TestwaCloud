package com.testwa.distest.client.util;

import java.io.File;
import java.nio.file.Paths;

public class Constant {
//	private static final String ROOT = System.getProperty("user.dir");

	public static final String APP_URL = "%s/download/app/%s";
	public static final String SCRIPT_URL = "%s/download/script/%s";

	public static final String localAppPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "app").toString();
	public static final String localScriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "script").toString();
	public static final String localScriptTmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "script_tmp").toString();

	public static final String localAppiumLogPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "appium").toString();
	public static final String localScreenshotPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "screenshot").toString();
	public static final String localLogcatPath = Paths.get(System.getProperty("java.io.tmpdir"), "testwa_agent", "logcat").toString();



	public static File getMinicap() {
		return new File(Constant.class.getResource(File.separator + "minicap").getPath());
	}

	public static String getMinicapBin() {
//		return new File(Constant.class.getResource(File.separator + "minicap" + File.separator + "libs").getPath());
		return Constant.class.getResource(File.separator + "minicap" + File.separator + "libs").getPath();
	}

	public static String getMinicapSo() {
//		return new File(Constant.class.getResource(File.separator + "minicap" + File.separator + "shared").getPath());
		return Constant.class.getResource(File.separator + "minicap" + File.separator + "shared").getPath();
	}

}