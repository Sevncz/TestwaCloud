package com.testwa.distest.client.android.util;

import java.io.IOException;
import java.util.TreeSet;

import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.distest.client.android.AndroidHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MinicapUtilTest {
	private static MiniCapUtil minicap = null;

	@BeforeClass
	public static void setUp() throws InterruptedException {
		TreeSet<AndroidDevice> devices = AndroidHelper.getInstance().getAllDevices();
		Assert.assertTrue(devices.size() > 0);
		minicap = new MiniCapUtil(devices.first().getDevice(), "127.0.0.1", 5566);

	}

	@Test
	public void takeScreenShotOnceTest() {
		long start = System.currentTimeMillis();
		minicap.takeScreenShotOnce();
		long current = System.currentTimeMillis();
		System.out.println(current-start);
		start = System.currentTimeMillis();
	}

	@Test
	public void taktest() throws IOException {
		Runtime.getRuntime()
				.exec("adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1440x2560@1440x2560/0 -s > /data/local/tmp/screen.jpg");
	}

}