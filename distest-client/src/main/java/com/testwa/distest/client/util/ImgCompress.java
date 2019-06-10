package com.testwa.distest.client.util;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wen
 * @create 2019-04-29 20:21
 */
public class ImgCompress {

    public static byte[] decompressPicByte(byte[] picByte, float defaultScale) {
        ByteArrayInputStream intputStream = new ByteArrayInputStream(picByte);
        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(intputStream).scale(defaultScale);
        try {
            BufferedImage bufferedImage = builder.asBufferedImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", baos);
            byte[] byteArray = baos.toByteArray();
            return byteArray;
        } catch (IOException e) {
        }
        return picByte;
    }
}
