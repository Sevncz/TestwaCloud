package com.testwa.distest.server.service.issue.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.IssueLabelCondition;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.IssueLabelMapper;
import com.testwa.distest.server.mapper.IssueMapper;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
public class IssueService extends BaseService<Issue, Long> {

    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private IssueLabelMapper issueLabelMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRED)
    public long save(IssueNewForm form, Long projectId) {
        Issue issue = new Issue();
        issue.setProjectId(projectId);
        issue.setTitle(form.getTitle());
        issue.setContent(form.getContent());
        // 如果没有指定用户，则指定创建者本人
        if(form.getAssigneeId() != null) {
            issue.setAssigneeId(form.getAssigneeId());
        }else{
            issue.setAssigneeId(currentUser.getId());
        }
        String labelName = form.getLabelName();
        IssueLabel label = issueLabelMapper.getByName(projectId, labelName);
        if(label == null) {
            throw new BusinessException(ResultCode.INVALID_PARAM, "不存在的标签名称");
        }
        issue.setLabelId(label.getId());
        issue.setAuthorId(currentUser.getId());
        issue.setCreateTime(new Date());
        issue.setState(DB.IssueStateEnum.OPEN);

        issue.setEnabled(true);

        return issueMapper.insert(issue);
    }

    public PageInfo<Issue> page(IssueListForm form, Long projectId) {
        Issue query = new Issue();
        query.setProjectId(projectId);

        if(form.getAuthorId() != null) {
            query.setAuthorId(form.getAuthorId());
        }
        if(form.getAssigneeId() != null) {
            query.setAssigneeId(form.getAssigneeId());
        }
        if(StringUtils.isNotBlank(form.getLabelName())) {
            IssueLabelCondition condition = new IssueLabelCondition();
            condition.setProjectId(projectId);
            condition.setName(form.getLabelName());
            List<IssueLabel> labels = issueLabelMapper.selectByCondition(condition);
            if(labels.isEmpty()) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在的标签名称");
            }
            IssueLabel label = labels.get(0);
            query.setLabelId(label.getId());
        }
        if(StringUtils.isNotBlank(form.getState())) {
            DB.IssueStateEnum stateEnum = DB.IssueStateEnum.nameOf(form.getState());
            if(stateEnum == null) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在状态");
            }
            query.setState(stateEnum);
        }
        //分页处理
        PageHelper.startPage(form.getPageNo(), form.getPageSize());
        PageHelper.orderBy(form.getOrderBy() + " " + form.getOrder());
        List<Issue> issues;
        if(StringUtils.isNotBlank(form.getIssueSearch())) {
            issues = issueMapper.search(query, form.getIssueSearch());
        }else{
            issues = issueMapper.findBy(query);
        }
        return new PageInfo(issues);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateState(Long issueId, DB.IssueStateEnum stateEnum) {
        Issue issue = get(issueId);
        if(issue != null) {
            issue.setState(stateEnum);
            issueMapper.update(issue);
        }
    }

}
