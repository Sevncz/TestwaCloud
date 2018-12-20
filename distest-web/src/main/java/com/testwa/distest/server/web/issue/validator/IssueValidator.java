package com.testwa.distest.server.web.issue.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.service.issue.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssueValidator {

    @Autowired
    private IssueService issueService;


    public Issue validateIssueExist(Long issueId) {
        Issue issue = issueService.get(issueId);
        if(issue == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "issue不存在");
        }
        return issue;
    }


}
