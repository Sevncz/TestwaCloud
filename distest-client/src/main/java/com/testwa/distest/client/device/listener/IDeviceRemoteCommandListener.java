package com.testwa.distest.client.device.listener;

import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriver;
import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;
import com.testwa.distest.client.device.listener.callback.RemoteCommandCallBackUtils;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wen
 * @create 2019-05-23 21:43
 */
@Slf4j
public class IDeviceRemoteCommandListener implements StreamObserver<Message> {
    private AtomicBoolean isOnNext = new AtomicBoolean(false);
    private AtomicBoolean isOnErrot = new AtomicBoolean(false);
    private String deviceId;
    private IDeviceRemoteControlDriver driver;
    private volatile ConcurrentHashMap<Message.Topic, IRemoteCommandCallBack> cache = new ConcurrentHashMap<>();

    public IDeviceRemoteCommandListener(String serial, IDeviceRemoteControlDriver driver) {
        this.deviceId = serial;
        this.driver = driver;
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("{} register server error ... ... msg: [{}]", deviceId, throwable.getMessage());
        isOnErrot.set(true);
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        driver.register();
    }

    @Override
    public void onCompleted() {

    }


    @Override
    public void onNext(Message message) {
        log.debug(deviceId +",{topicName:"+message.getTopicName()+",getSource:"+message.getStatus()+"}");
        if(Message.Topic.CONNECTED.equals(message.getTopicName())) {
            isOnNext.set(true);
            return;
        }
        IRemoteCommandCallBack call;
        try {
            if(cache.containsKey(message.getTopicName())){
                call = cache.get(message.getTopicName());
            }else{
                call = RemoteCommandCallBackUtils.getCallBack(message.getTopicName(), driver);
                cache.put(message.getTopicName(), call);
            }
            call.callback(message.getMessage());
        } catch (Exception e) {
            log.error("回调错误", e);
        }
    }

    /**
     * 最多等待5s
     * @return
     */
    public boolean isConnected() {
        int tryTime = 5;
        while(tryTime > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

            }
            if(isOnNext.get()) {
                break;
            }
            tryTime--;
        }
        return isOnNext.get();
    }
}
