package com.testwa.distest.client.appium.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 16/8/26.
 */
public class AppiumCache {

    public static final Map<String, AppiumParallelTest> apt = new HashMap<>();
    public static final Map<String, String> url = new HashMap<>();
    public static final Set<String> device_running = new HashSet<>();

    private BlockingQueue<Object> task = new ArrayBlockingQueue<Object>(5);

}
