package com.testwa.distest.server.web.issue.vo;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-21 13:53
 */
@Data
public class IssueVO {
    private Long id;
    private Long projectId;
    private Date createTime;
    private UserVO assignee;
    private UserVO author;
    // 标题
    private String title;
    // 内容
    private String content;
    // 状态
    private DB.IssueStateEnum state;

    private List<IssueLabelVO> labels;

}
