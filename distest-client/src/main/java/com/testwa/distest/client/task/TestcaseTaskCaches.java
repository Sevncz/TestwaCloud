package com.testwa.distest.client.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 2016/10/2.
 */
public class TestcaseTaskCaches {

    private static final Map<String, Testcase> caches = new ConcurrentHashMap<>();

    public static void add(String serial, Testcase tc){
        caches.put(serial, tc);
    }

    public static void remove(String serial){
        caches.remove(serial);
    }

    public static Testcase getTCBySerial(String serial){
        return caches.get(serial);
    }

}
