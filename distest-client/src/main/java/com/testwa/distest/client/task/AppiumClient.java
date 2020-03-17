package com.testwa.distest.client.task;

import com.testwa.distest.client.util.PortUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.Data;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Data
public class AppiumClient {
    AndroidDriver<WebElement> driver;

    /**
     * 'app': os.path.abspath('${appPath}'),
     * 'automationName': 'UIAutomator2',
     * 'platformName': 'Android',
     * 'platformVersion': '${platformVersion}',
     * 'deviceName': '${deviceName}',
     * 'autoGrantPermissions': 'true',
     * 'systemPort': '${systemPort}',
     * 'autoLaunch': 'false',
     *
     * @param deviceId
     * @param port
     * @param appPath
     * @throws MalformedURLException
     */
    public void init(String deviceId, String port, String appPath, String platformVersion) throws IOException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceId);
        capabilities.setCapability("platformName", MobilePlatform.ANDROID);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, platformVersion);
        capabilities.setCapability(MobileCapabilityType.APP, appPath);
        capabilities.setCapability("systemPort", PortUtil.getAvailablePort() + "");
        capabilities.setCapability("autoGrantPermissions", "true");
        capabilities.setCapability("autoLaunch", "false");
        // driver实例
        driver = new AndroidDriver<WebElement>(new URL("http://127.0.0.1:" + port + "/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

}
