package com.testwa.distest.server.web.issue.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueAssignee;
import com.testwa.distest.server.service.issue.service.IssueAssigneeService;
import com.testwa.distest.server.service.issue.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssueAssigneeValidator {

    @Autowired
    private IssueAssigneeService issueAssigneeService;


    public void validateAssigneeNotInIssue(Long issueId, Long assigneeId) {
        IssueAssignee issueAssignees = issueAssigneeService.getByIssueIdAndAssigneeId(issueId, assigneeId);
        if(issueAssignees != null){
            throw new BusinessException(ResultCode.CONFLICT, "assignee 在 issue 已存在");
        }
    }

    public IssueAssignee validateAssigneeInIssue(Long issueId, Long assigneeId) {
        IssueAssignee issueAssignees = issueAssigneeService.getByIssueIdAndAssigneeId(issueId, assigneeId);
        if(issueAssignees == null){
            throw new BusinessException(ResultCode.CONFLICT, "assignee 在 issue 不存在");
        }
        return issueAssignees;
    }
}
