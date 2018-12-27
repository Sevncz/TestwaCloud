package com.testwa.distest.server.web.issue.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.service.issue.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LabelValidator {

    @Autowired
    private LabelService labelService;

    public IssueLabel validateLabelExist(Long labelId) {

        IssueLabel label = labelService.get(labelId);
        if(label == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return label;
    }

    public void validateLabelNameExist(Long projectId, String name) {
        IssueLabel label = labelService.getByName(projectId, name);
        if(label == null){
            throw new BusinessException(ResultCode.CONFLICT, "标签" + name + "不存在");
        }
    }

    public void validateLabelNameNotExist(Long projectId, String name, Long labelId) {
        IssueLabel label = labelService.getByName(projectId, name);
        if(label != null && !label.getId().equals(labelId)){
            throw new BusinessException(ResultCode.CONFLICT, "标签" + name + "已存在");
        }
    }

    public void validateLabelNameNotExist(Long projectId, String name) {
        IssueLabel label = labelService.getByName(projectId, name);
        if(label != null){
            throw new BusinessException(ResultCode.CONFLICT, "标签" + name + "已存在");
        }
    }
}
