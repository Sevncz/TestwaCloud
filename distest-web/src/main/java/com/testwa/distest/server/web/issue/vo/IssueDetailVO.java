package com.testwa.distest.server.web.issue.vo;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-21 13:53
 */
@Data
public class IssueDetailVO {
    private Long id;
    private Long projectId;
    private Date createTime;
    private List<UserVO> assignees;
    private UserVO author;
    private Long commentNum;
    // 标题
    private String title;
    // 内容
    private String content;
    // 内容
    private String[] issueAttachments;
    // 状态
    private DB.IssueStateEnum state;
    // 优先级
    private DB.IssuePriorityEnum priority;

    private List<IssueLabelVO> labels;

    // 参与人员
    private List<UserVO> participants;
}
