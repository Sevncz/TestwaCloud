package com.testwa.distest.client.component;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Constant {

    // CPU架构的种类
    public static final String ABIS_ARM64_V8A = "arm64-v8a";
    public static final String ABIS_ARMEABI_V7A = "armeabi-v7a";
    public static final String ABIS_X86 = "x86";
    public static final String ABIS_X86_64 = "x86_64";

	public static final String PROP_ABI = "ro.product.cpu.abi";
	public static final String PROP_SDK = "ro.build.version.sdk";
    public static final String PROP_REL = "ro.build.version.release";

    public static final String MINICAP_BIN = "minicap";
    public static final String MINICAP_SO = "minicap.so";
    public static final String MINICAP_NOPIE = "minicap-nopie";
    public static final String MINICAP_DIR = "/data/local/tmp/minicap-devel";


    public static final String MINITOUCH_BIN = "minitouch";
    public static final String MINITOUCH_NOPIE = "minitouch-nopie";
    public static final String MINITOUCH_DIR = "/data/local/tmp/minitouch-devel";

	public static final String localAppPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "app").toString();
	public static final String localScriptPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "script").toString();
	public static final String localScriptTmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "script_tmp").toString();

	public static final String localAppiumLogPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "appium").toString();
	public static final String localScreenshotPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "screenshot").toString();
	public static final String localLogcatPath = Paths.get(System.getProperty("java.io.tmpdir"), "distest_agent", "logcat").toString();


    public static String getMinitouchBin(String abi, String bin) {
        return File.separator + "minitouch" + File.separator + abi + File.separator + bin;
    }

    public static File getTmpFile(String fileName) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpdir);
        tmp = new File(tmp, "distest-agent");
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        return new File(tmp, fileName);
    }

	public static String getMinicap() {
		return File.separator + "minicap";
	}

	public static String getMinicapBin() {
		return File.separator + "minicap" + File.separator + "libs";
	}

	public static String getMinicapSo() {
		return File.separator + "minicap" + File.separator + "shared";
	}

}