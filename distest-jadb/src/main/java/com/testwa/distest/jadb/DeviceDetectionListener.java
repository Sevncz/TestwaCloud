package com.testwa.distest.jadb;

import java.util.List;

public interface DeviceDetectionListener {
    void onDetect(List<JadbDevice> devices);
    void onException(Exception e);
}

