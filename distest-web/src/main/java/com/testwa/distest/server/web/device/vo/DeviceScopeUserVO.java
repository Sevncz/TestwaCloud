package com.testwa.distest.server.web.device.vo;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceScopeUserVO {
    private Long shareId;
    private Date startTime;
    private Date endTime;

    private Long userId;
    private String username;
}
