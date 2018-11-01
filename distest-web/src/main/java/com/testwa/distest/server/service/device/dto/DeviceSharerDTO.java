package com.testwa.distest.server.service.device.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceSharerDTO {
    private Long shareId;
    private Date startTime;
    private Date endTime;

    private Long userId;
    private String username;

}
