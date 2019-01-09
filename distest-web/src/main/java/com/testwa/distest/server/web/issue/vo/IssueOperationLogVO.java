package com.testwa.distest.server.web.issue.vo;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.Date;

/**
 * @author wen
 * @create 2019-01-08 18:22
 */
@Data
public class IssueOperationLogVO {
    private Long id;
    private String content;
    private DB.IssueOpTypeEnum opType;
    private UserVO user;
    private Date createTime;
}
