package com.testwa.distest.client.device.listener;

import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriver;
import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;
import com.testwa.distest.client.device.listener.callback.RemoteCommandCallBackUtils;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wen
 * @create 2019-05-23 21:43
 */
@Slf4j
public class IDeviceRemoteCommandListener implements StreamObserver<Message> {
    private String deviceId;
    private IDeviceRemoteControlDriver driver;
    private volatile ConcurrentHashMap<Message.Topic, IRemoteCommandCallBack> cache = new ConcurrentHashMap<>();

    public IDeviceRemoteCommandListener(String serial, IDeviceRemoteControlDriver driver) {
        this.deviceId = serial;
        this.driver = driver;
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("{} connect retry ... ...", deviceId);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.register();
    }

    @Override
    public void onCompleted() {

    }


    @Override
    public void onNext(Message message) {
        log.debug(deviceId +",{topicName:"+message.getTopicName()+",getSource:"+message.getStatus()+"}");
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


}
