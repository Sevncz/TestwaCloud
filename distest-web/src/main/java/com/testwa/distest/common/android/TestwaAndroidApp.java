package com.testwa.distest.common.android;

import com.github.cosysoft.device.android.impl.DefaultAndroidApp;
import com.github.cosysoft.device.shell.AndroidSdk;
import com.github.cosysoft.device.shell.AndroidSdkException;
import com.github.cosysoft.device.shell.ShellCommand;
import com.github.cosysoft.device.shell.ShellCommandException;
import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/8/30.
 */
public class TestwaAndroidApp extends DefaultAndroidApp {
    private File apkFile;
    private String sdkVersion;
    private String targetSdkVersion;


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
        } catch (Exception var6) {
            output = var6.getCause().getMessage();
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
}
