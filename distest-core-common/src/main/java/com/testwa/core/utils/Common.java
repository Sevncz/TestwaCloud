package com.testwa.core.utils;

import java.net.ServerSocket;
import java.util.Arrays;

/**
 * Created by wen on 03/06/2017.
 */
public class Common {

    public static byte[] subArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[end - start];
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    public static byte[] mergeArray(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

}
