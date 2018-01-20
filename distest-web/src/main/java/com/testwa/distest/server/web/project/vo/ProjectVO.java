package com.testwa.distest.server.web.project.vo;

import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 20/10/2017.
 */
@Data
public class ProjectVO {

    private Long id;
    private String projectName;
    private String description;
    private Date createTime;

    private UserVO createUser;

}
