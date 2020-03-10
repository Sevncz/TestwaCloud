package com.testwa.distest.client.config;

import com.testwa.distest.client.component.executor.worker.FunctionalPythonExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheProperty {
    private static String resourcePath;
    private static Map<String, FunctionalPythonExecutor> deviceExcutorMap = new ConcurrentHashMap<>();

    public static void setResourcePath(String resourcePath) {
        CacheProperty.resourcePath = resourcePath;
    }

    public static String getResourcePath() {
        return CacheProperty.resourcePath;
    }

    public static void putDeviceExcutorMap(String deviceId, FunctionalPythonExecutor excutor) {
        CacheProperty.deviceExcutorMap.put(deviceId, excutor);
    }

    public static FunctionalPythonExecutor getDeviceExcutorMap(String deviceId) {
        return CacheProperty.deviceExcutorMap.get(deviceId);
    }

    public static void removeDeviceExcutorMap(String deviceId) {
        CacheProperty.deviceExcutorMap.remove(deviceId);
    }
}
