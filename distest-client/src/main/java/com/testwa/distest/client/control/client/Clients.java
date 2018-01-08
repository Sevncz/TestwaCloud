package com.testwa.distest.client.control.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.rpc.testwa.device.DeviceServiceGrpc;
import io.rpc.testwa.task.TaskServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 10/06/2017.
 */
public class Clients {

    private static Map<String, RemoteClient> all = new HashMap<>();

    public static void add(String serial, RemoteClient client){
        all.put(serial, client);
    }

    public static RemoteClient get(String serial){
        return all.get(serial);
    }

    public static List<RemoteClient> all(){
        return new ArrayList<>(all.values());
    }

    public static void remove(String serial){
        RemoteClient rc = all.get(serial);
        rc.stop();
        all.remove(serial);
    }

}
