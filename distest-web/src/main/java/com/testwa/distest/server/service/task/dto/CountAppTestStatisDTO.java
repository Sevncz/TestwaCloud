package com.testwa.distest.server.service.task.dto;

import lombok.Data;

@Data
public class CountAppTestStatisDTO {

    private Long count;
    private Long appId;
    private Integer taskType;

}
