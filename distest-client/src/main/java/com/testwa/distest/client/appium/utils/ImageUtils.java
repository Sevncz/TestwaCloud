package com.testwa.distest.client.appium.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by saikrisv on 17/03/16.
 */
public class ImageUtils {
    public void wrapDeviceFrames(String deviceFrame, String deviceScreenToBeFramed,
        String framedDeviceScreen) throws InterruptedException, IOException, IM4JavaException {
        IMOperation op = new IMOperation();
        ConvertCmd cmd = new ConvertCmd();
        op.addImage(deviceFrame);
        op.addImage(deviceScreenToBeFramed);
        op.gravity("center");
        op.composite();
        op.opaque("none");
        op.addImage(framedDeviceScreen);
        cmd.run(op);
    }


    public void createJSonForHtml() throws IOException {
        File dir = new File(System.getProperty("auth.dir") + "/target/screenshot/");
        System.out.println("Getting all files in " + dir.getCanonicalPath()
            + " including those in subdirectories");
        List<File> files =
            (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        JsonArray mainObj = new JsonArray();

        JsonObject jsonObject = new JsonObject();

        String deviceName;
        FileWriter file1 = new FileWriter("test.json");
        for (File file : files) {
            JsonObject ja;

            if (file.getCanonicalFile().toString().contains("results")) {
                ja = new JsonObject();
                deviceName = file.getName().split("_")[0];
                String deviceModel = file.getName().split("_")[1];
                String screenName = file.getName().split("_")[2];
                String imagePath = file.getPath().toString();
                ja.addProperty("Device Name", deviceName);
                ja.addProperty("Device Model", deviceModel);
                ja.addProperty("Screen Name", screenName);
                ja.addProperty("Image Path", imagePath);
                jsonObject.add(deviceName, ja);

            }

        }
        mainObj.add(jsonObject);
        file1.write(mainObj.toString());
        file1.flush();
        file1.close();
    }

    public static void createAnimatedGif(List<File> testScreenshots, File animatedGif) throws IOException {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(animatedGif.getAbsolutePath());
        encoder.setDelay(1500 /* 1.5 seconds */);
        encoder.setQuality(3 /* highest */);
        encoder.setRepeat(0 /* infinite */);
        encoder.setTransparent(Color.WHITE);

        int width = 0;
        int height = 0;
        for (File testScreenshot : testScreenshots) {
            BufferedImage bufferedImage = ImageIO.read(testScreenshot);
            width = Math.max(bufferedImage.getWidth(), width);
            height = Math.max(bufferedImage.getHeight(), height);
        }
        encoder.setSize(width, height);

        for (File testScreenshot : testScreenshots) {
            encoder.addFrame(ImageIO.read(testScreenshot));
        }

        encoder.finish();
    }

    public static void createGif() throws IOException {
        File[] files = new File(System.getProperty("auth.dir") + "/target/screenshot/").listFiles();
        showFiles(files);
    }

    public static void showFiles(File[] files) throws IOException {
        int imageAdded = 0;
        List<File> gifDevices = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getName());
                showFiles(file.listFiles()); // Calls same method again.
            } else {
                System.out.println("File: " + file.getName());
                int length = stringContainsItemFromList("results",
                    Arrays.asList(file.getParentFile().list()));
                if (file.getName().contains("results")) {
                    gifDevices.add(file);
                    imageAdded++;
                    if (imageAdded == length) {
                        System.out.println("Create Gif");
                        String GifFileName =
                            file.getParent().substring(file.getParent().lastIndexOf("/") + 1);
                        createAnimatedGif(gifDevices,
                            new File(file.getParent() + "/" + GifFileName + ".gif"));
                    }
                }

            }
        }
    }

    public static int stringContainsItemFromList(String inputString, List<String> items) {
        int j = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).contains(inputString)) {
                j++;
            }
        }
        return j;
    }
}

