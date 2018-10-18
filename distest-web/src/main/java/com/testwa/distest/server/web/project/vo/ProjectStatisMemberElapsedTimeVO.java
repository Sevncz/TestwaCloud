package com.testwa.distest.server.web.project.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class ProjectStatisMemberElapsedTimeVO {

    private String username;
    private ProjectStatisElapsedTimeLineVO lines;

}
