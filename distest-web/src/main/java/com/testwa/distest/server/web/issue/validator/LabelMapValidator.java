package com.testwa.distest.server.web.issue.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.IssueLabelMap;
import com.testwa.distest.server.service.issue.service.LabelMapService;
import com.testwa.distest.server.service.issue.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LabelMapValidator {

    @Autowired
    private LabelMapService labelMapService;

    public void validateLabelNotInIssue(Long issueId, Long labelId) {
        IssueLabelMap entity = labelMapService.getByIssueIdAndLabelId(issueId, labelId);
        if(entity != null){
            throw new BusinessException(ResultCode.CONFLICT, "label 在 issue 已存在");
        }
    }

    public IssueLabelMap validateLabelInIssue(Long issueId, Long labelId) {
        IssueLabelMap entity = labelMapService.getByIssueIdAndLabelId(issueId, labelId);
        if(entity == null){
            throw new BusinessException(ResultCode.CONFLICT, "label 在 issue 不存在");
        }
        return entity;
    }
}
