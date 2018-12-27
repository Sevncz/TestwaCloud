package com.testwa.distest.server.web.issue.vo;

import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.Date;

/**
 * @author wen
 * @create 2018-12-26 17:39
 */
@Data
public class IssueCommentVO {

    private Long id;
    private String content;
    private UserVO author;
    private Date createTime;
}
