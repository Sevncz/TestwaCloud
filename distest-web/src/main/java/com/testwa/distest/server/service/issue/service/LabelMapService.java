package com.testwa.distest.server.service.issue.service;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.async.IssueLogTask;
import com.testwa.distest.server.condition.BaseProjectCondition;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.IssueLabelDict;
import com.testwa.distest.server.entity.IssueLabelMap;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.IssueLabelDictMapper;
import com.testwa.distest.server.mapper.IssueLabelMapMapper;
import com.testwa.distest.server.mapper.IssueLabelMapper;
import com.testwa.distest.server.service.issue.form.IssueLabelNewForm;
import com.testwa.distest.server.service.issue.form.IssueLabelUpdateForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Program: distest
 * @Description:
 * @Author: wen
 * @Create: 2018-11-21 11:47
 **/
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class LabelMapService extends BaseService<IssueLabelMap, Long> {

    @Autowired
    private IssueLabelMapMapper issueLabelMapMapper;
    @Autowired
    private IssueLabelMapper labelMapper;
    @Autowired
    private IssueLogTask issueLogTask;
    @Autowired
    private User currentUser;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long issueId, Long labelId) {
        IssueLabel label = labelMapper.selectById(labelId);
        if(label == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        issueLabelMapMapper.deleteByIssueIdAndLabel(issueId, labelId);
        // 保存操作日志
        issueLogTask.logRemoveForLabel(issueId, currentUser.getId(), label.getName());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long issueId, Long labelId) {
        IssueLabel label = labelMapper.selectById(labelId);
        if(label == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        IssueLabelMap labelMap = new IssueLabelMap();
        labelMap.setIssueId(issueId);
        labelMap.setLabelId(labelId);
        issueLabelMapMapper.insert(labelMap);
        // 保存操作日志
        issueLogTask.logAddForLabel(issueId, currentUser.getId(), label.getName());
    }

    public IssueLabelMap getByIssueIdAndLabelId(Long issueId, Long labelId) {
        return issueLabelMapMapper.getByIssueIdAndLabelId(issueId, labelId);
    }
}

