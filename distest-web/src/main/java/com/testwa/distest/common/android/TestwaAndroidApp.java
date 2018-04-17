package com.testwa.distest.common.android;

import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.github.cosysoft.device.shell.AndroidSdkException;
import com.github.cosysoft.device.shell.ShellCommand;
import com.github.cosysoft.device.shell.ShellCommandException;
import com.testwa.distest.common.shell.AbstractCommonExecs;
import com.testwa.distest.common.shell.ExecResult;
import com.testwa.distest.common.shell.UTF8CommonExecs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/8/30.
 */
@Slf4j
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
        String output = aaptCmd();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(output);
        return matcher.find()?matcher.group(1):null;
    }

    private String aaptCmd() {
        CommandLine line = new CommandLine(AndroidSdk.aapt());
        line.addArgument("dump", false);
        line.addArgument("badging", false);
        line.addArgument(this.apkFile.getAbsolutePath(), false);
        UTF8CommonExecs executable = new UTF8CommonExecs(line);
        String output = "";
        try {
            output = executable.exec();
        } catch (IOException e) {
            output = e.getCause().getMessage();
        }
        log.info("aapt output ======= {}", output);
        return output;
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

                String regex = "application: label=\'(.*?)\' icon=\'(.*?)\'";
                String aaptResult = this.aaptCmd();
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(aaptResult);
                this.applicationIcon = matcher.find()?matcher.group(2):null;

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
                    String regex = "application: label=\'(.*?)\' icon=\'(.*?)\'";
                    this.applicationLable = this.extractApkDetails(regex);
                }
                log.info("this.applicationLable ======== {}", this.applicationLable);
            } catch (ShellCommandException var2) {
                throw new RuntimeException("The application icon of the apk " + this.apkFile.getName() + " cannot be extracted.");
            }
        }
        return this.applicationLable;
    }

    public static void main(String[] args) {
        String regex = "application: label=\'(.*)\' icon=\'(.*?)\'";
        String output = "application: label='ofo共享单车' icon='res/mipmap-hdpi-v4/application_icon.png'";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(output);
        if(matcher.find()){
            System.out.println(matcher.group(1));
        }
    }
}
