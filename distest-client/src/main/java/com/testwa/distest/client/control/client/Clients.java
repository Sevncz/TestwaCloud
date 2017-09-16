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
@Component
public class Clients {

    @Value("${grpc.host}")
    private String GrpcHost;
    private static String grpcHost;
    @Value("${grpc.port}")
    private Integer GrpcPort;
    private static Integer grpcPort;

    @PostConstruct
    private void init() {
        grpcHost = this.GrpcHost;
        grpcPort = this.GrpcPort;
    }

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
        all.remove(serial);
    }

    public static DeviceServiceGrpc.DeviceServiceFutureStub deviceService() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext(true)
                .build();
        final DeviceServiceGrpc.DeviceServiceFutureStub stub = DeviceServiceGrpc.newFutureStub(channel);
        return stub;
    }

    public static DeviceServiceGrpc.DeviceServiceFutureStub deviceService(String webHost, Integer webPort) {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(webHost, webPort)
                .usePlaintext(true)
                .build();
        final DeviceServiceGrpc.DeviceServiceFutureStub stub = DeviceServiceGrpc.newFutureStub(channel);
        return stub;
    }

    public static TaskServiceGrpc.TaskServiceFutureStub taskService() {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext(true)
                .build();
        final TaskServiceGrpc.TaskServiceFutureStub stub = TaskServiceGrpc.newFutureStub(channel);
        return stub;
    }

}
