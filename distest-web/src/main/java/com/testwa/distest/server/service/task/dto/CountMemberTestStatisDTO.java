package com.testwa.distest.server.service.task.dto;

import lombok.Data;

@Data
public class CountMemberTestStatisDTO {

    private Long count;
    private Long memberId;
    private Integer taskType;

}
