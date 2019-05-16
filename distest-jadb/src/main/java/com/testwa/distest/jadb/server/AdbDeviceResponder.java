package com.testwa.distest.jadb.server;

import com.testwa.distest.jadb.JadbException;
import com.testwa.distest.jadb.RemoteFile;

import java.io.*;
import java.util.List;

/**
 * Created by vidstige on 20/03/14.
 */
public interface AdbDeviceResponder {
    String getSerial();
    String getType();

    void filePushed(RemoteFile path, int mode, ByteArrayOutputStream buffer) throws JadbException;
    void filePulled(RemoteFile path, ByteArrayOutputStream buffer) throws JadbException, IOException;

    void shell(String command, DataOutputStream stdout, DataInput stdin) throws IOException;
    void enableIpCommand(String ip, DataOutputStream outputStream) throws IOException;

    List<RemoteFile> list(String path) throws IOException;
}
