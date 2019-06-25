package com.testwa.distest.client.event;

import com.testwa.distest.jadb.JadbDevice;
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
    private JadbDevice jadbDevice;
    private DeviceStatusChangeRequest.LineStatus lineStatus;

    public DeviceStatusChangeEvent(Object source, JadbDevice jadbDevice, DeviceStatusChangeRequest.LineStatus lineStatus) {
        super(source);
        this.jadbDevice = jadbDevice;
        this.lineStatus = lineStatus;
    }
}
