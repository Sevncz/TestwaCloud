package com.testwa.distest.server.web.project.vo;

import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class ProjectDetailVO {
    private ProjectVO project;
    private List<UserVO> projectMembers = new ArrayList<>();
}
