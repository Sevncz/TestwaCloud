package com.testwa.distest.client.event;import lombok.Data;import org.springframework.context.ApplicationEvent;@Datapublic class DeviceConnectedEvent extends ApplicationEvent {    private String deviceId;    public DeviceConnectedEvent(Object source, String deviceId){        super(source);        this.deviceId = deviceId;    }}