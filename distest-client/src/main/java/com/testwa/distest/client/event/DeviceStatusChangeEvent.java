package com.testwa.distest.client.event;

import io.rpc.testwa.device.DeviceStatusChangeRequest;
import lombok.Data;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @author wen
 * @create 2019-05-15 20:12
 */
@ToString
@Data
public class DeviceStatusChangeEvent extends ApplicationEvent {
    private String deviceId;
    private DeviceStatusChangeRequest.LineStatus lineStatus;

    public DeviceStatusChangeEvent(Object source, String deviceId, DeviceStatusChangeRequest.LineStatus lineStatus) {
        super(source);
        this.deviceId = deviceId;
        this.lineStatus = lineStatus;
    }
}
