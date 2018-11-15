package com.testwa.distest.server.web.project.vo;

import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class ProjectMemberVO {

    private Long projectId;
    private Long memberId;
    private DB.ProjectRole projectRole;

}
