package com.testwa.distest.client.minicap;

import com.android.ddmlib.IShellOutputReceiver;
import com.testwa.core.utils.Common;

import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by harry on 2017/4/17.
 */
public class BinaryOutputReceiver implements IShellOutputReceiver {

    byte[] output = new byte[0];

    @Override
    public void addOutput(byte[] bytes, int offest, int len) {
        byte[] b = Arrays.copyOfRange(bytes, offest, offest + len);
        output = Common.mergeArray(output, b);
    }

    @Override
    public void flush() {
        System.out.println("flush");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public byte[] getOutput() {
        return output;
    }
}
