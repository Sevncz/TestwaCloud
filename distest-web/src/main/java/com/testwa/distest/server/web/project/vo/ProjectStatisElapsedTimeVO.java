package com.testwa.distest.server.web.project.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@ToString
public class ProjectStatisElapsedTimeVO {

    private ProjectStatisElapsedTimeLineVO all;
    private List<ProjectStatisMemberElapsedTimeVO> members;



}
