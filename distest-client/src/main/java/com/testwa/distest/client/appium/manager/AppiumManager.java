package com.testwa.distest.client.appium.manager;

import com.testwa.core.utils.Identities;
import com.testwa.distest.client.appium.utils.Config;
import com.testwa.distest.client.util.Constant;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.AndroidServerFlag;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.appium.java_client.service.local.flags.ServerArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Appium Manager - this class contains method to start and stops appium server
 * To execute the tests from eclipse, you need to set PATH as
 * /usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin in run configuration
 */
public class AppiumManager {
    private static Logger LOG = LoggerFactory.getLogger(AppiumManager.class);

    AvailabelPorts ap = new AvailabelPorts();
    public AppiumDriverLocalService appiumDriverLocalService;
    /**
     * start appium with auto generated ports : appium port, chrome port,
     * bootstrap port and device UDID
     */

    public AppiumDriverLocalService appiumServerForAndroid(String deviceID, String installApp, String testcaseDetailId)
        throws Exception {
        LOG.info("Starting Appium Server Android, {}", deviceID);
        int port = ap.getPort();
        int chromePort = ap.getPort();
        int bootstrapPort = ap.getPort();
        int selendroidPort = ap.getPort();

        String appiumLogPath = getAppiumLogPath(deviceID);

        AppiumServiceBuilder builder =
            new AppiumServiceBuilder()
                    .usingDriverExecutable(new File(Config.getString("node.excute.path")))
                    .withAppiumJS(new File(Config.getString("appium.js.path")))
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "info").withLogFile(new File(appiumLogPath))
                    .withArgument(AndroidServerFlag.CHROME_DRIVER_PORT, Integer.toString(chromePort))
                    .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER,
                            Integer.toString(bootstrapPort))
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                    .withArgument(AndroidServerFlag.SUPPRESS_ADB_KILL_SERVER)
                    .withArgument(AndroidServerFlag.SELENDROID_PORT, Integer.toString(selendroidPort))
                    .withArgument(CustomServerFlag.INSTALL_APP, installApp)
                    .withArgument(CustomServerFlag.TESTCASE_LOG_ID, testcaseDetailId)
                    .withArgument(CustomServerFlag.TESTWA_DEVICE_ID, deviceID)
                    .withArgument(CustomServerFlag.SCREEN_PATH, Constant.localScreenshotPath)
                    .withArgument(CustomServerFlag.SET_DEVICE, deviceID)
                    .usingPort(port);
        /* and so on */
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        return appiumDriverLocalService;

    }

    private String getAppiumLogPath(String deviceID) throws IOException {
        Path appiumLogDir = Paths.get(Constant.localAppiumLogPath, deviceID.replaceAll("\\W", "_"));
        if(!Files.exists(appiumLogDir)){
            Files.createDirectories(appiumLogDir);
        }
        return Paths.get(appiumLogDir.toString(), String.format("appium_%s.log", Identities.randomLong())).toString();
    }

    /**
     * start appium with auto generated ports : appium port, chrome port,
     * bootstrap port and device UDID
     */
    ServerArgument webKitProxy = new ServerArgument() {
        @Override public String getArgument() {
            return "--webkit-debug-proxy-port";
        }
    };

    public AppiumDriverLocalService appiumServerForIOS(String deviceID,
        String webKitPort) throws Exception {
        System.out.println("Starting Appium Server IOS");
        int port = ap.getPort();
        int chromePort = ap.getPort();

        String appiumLogPath = getAppiumLogPath(deviceID);

        AppiumServiceBuilder builder =
            new AppiumServiceBuilder().withAppiumJS(new File(Config.getString("appium.js.path")))
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                    .withLogFile(new File(appiumLogPath))
//                  .withArgument(GeneralServerFlag.UIID, deviceID)
//                  .withArgument(IOSServerFlag.USE_NATIVE_INSTRUMENTS)
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "debug")
                    .withArgument(GeneralServerFlag.TEMP_DIRECTORY,
                            Constant.localAppiumLogPath + File.separator + "tmp_"
                            + port)
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                    .withArgument(CustomServerFlag.INSTALL_APP, "true")
                    .withArgument(CustomServerFlag.TESTCASE_LOG_ID, "1")
                    .withArgument(CustomServerFlag.TESTWA_DEVICE_ID, "1000")
                    .withArgument(CustomServerFlag.SCREEN_PATH, Constant.localScreenshotPath)
                    .usingPort(port);

		/* and so on */
        ;
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        return appiumDriverLocalService;

    }

    public URL getAppiumUrl() {
        return appiumDriverLocalService.getUrl();
    }

    public void destroyAppiumNode() {
        LOG.info("AppiumServer shutdown ing");
        appiumDriverLocalService.stop();
        if (appiumDriverLocalService.isRunning()) {
            LOG.info("AppiumServer didn't shut... Trying to quit again....");
            appiumDriverLocalService.stop();
        }
    }

}
