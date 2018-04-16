package com.testwa.distest.common.android;

import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.github.cosysoft.device.shell.AndroidSdkException;
import com.github.cosysoft.device.shell.ShellCommand;
import com.github.cosysoft.device.shell.ShellCommandException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/8/30.
 */
public class TestwaAndroidApp extends DefaultAndroidApp {
    public static final String APPLICATION_ICON_120 = "application-icon-120";
    public static final String APPLICATION_ICON_160 = "application-icon-160";
    public static final String APPLICATION_ICON_240 = "application-icon-240";
    public static final String APPLICATION_ICON_320 = "application-icon-320";
    public static final List<String> iconList = Arrays.asList(APPLICATION_ICON_120, APPLICATION_ICON_160, APPLICATION_ICON_240, APPLICATION_ICON_320);

    private File apkFile;
    private String sdkVersion;
    private String targetSdkVersion;
    private String applicationIcon;
    private String applicationLable;


    public TestwaAndroidApp(File apkFile) {
        super(apkFile);
        this.apkFile = apkFile;
    }

    private String extractApkDetails(String regex) throws ShellCommandException, AndroidSdkException {
        CommandLine line = new CommandLine(AndroidSdk.aapt());
        line.addArgument("dump", false);
        line.addArgument("badging", false);
        line.addArgument(this.apkFile.getAbsolutePath(), false);
        String output = "";

        try {
            output = ShellCommand.exec(line, 20000L);
        } catch (Exception e) {
            output = e.getCause().getMessage();
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(output);
        return matcher.find()?matcher.group(1):null;
    }

    public String getTargetSdkVersion(){
        if(this.targetSdkVersion == null) {
            try {
                this.targetSdkVersion = this.extractApkDetails("targetSdkVersion:\'(.*?)\'");
            } catch (ShellCommandException var2) {
                throw new RuntimeException("The main activity of the apk " + this.apkFile.getName() + " cannot be extracted.");
            }
        }
        return this.targetSdkVersion;
    }

    public String getSdkVersion(){
        if(this.sdkVersion == null) {
            try {
                this.sdkVersion = this.extractApkDetails("sdkVersion:\'(.*?)\'");
            } catch (ShellCommandException var2) {
                throw new RuntimeException("The main activity of the apk " + this.apkFile.getName() + " cannot be extracted.");
            }
        }
        return this.sdkVersion;
    }

    public String getApplicationIcon(){
        if(this.applicationIcon == null) {
            for(String icon : iconList){
                try {
                    this.applicationIcon = this.extractApkDetails(icon + ":\'(.*?)\'");
                    if(StringUtils.isNotBlank(this.applicationIcon)){
                        break;
                    }
                } catch (ShellCommandException var2) {
                    throw new RuntimeException("The application icon of the apk " + this.apkFile.getName() + " cannot be extracted.");
                }
            }
            if(StringUtils.isBlank(this.applicationIcon)){

                String line = this.extractApkDetails("application:\'(.*?)\'");
                if(StringUtils.isNotBlank(line)){
                    String[] rs = line.split("( icon=')|'");
                    //linux下获取应用名称
                    this.applicationIcon = rs[rs.length - 1];
                }
            }
        }
        return this.applicationIcon;
    }
    public String getApplicationLable(){
        // application: label='ofo共享单车' icon='res/mipmap-hdpi-v4/application_icon.png'
        if(this.applicationLable == null) {
            try {
                this.applicationLable = this.extractApkDetails("application-label:\'(.*?)\'");
                if(StringUtils.isBlank(this.applicationLable)){

                    String line = this.extractApkDetails("application:\'(.*?)\'");
                    if(StringUtils.isNotBlank(line)){
                        String[] rs = line.split("( icon=')|'");
                        //linux下获取应用名称
                        this.applicationLable = rs[1];
                    }
                }
            } catch (ShellCommandException var2) {
                throw new RuntimeException("The application icon of the apk " + this.apkFile.getName() + " cannot be extracted.");
            }
        }
        return this.applicationLable;
    }
}
