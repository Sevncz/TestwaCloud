package com.testwa.distest.server.service.issue.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.condition.IssueLabelCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.*;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.form.IssueUpdateForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private IssueLabelMapper labelMapper;
    @Autowired
    private IssueLabelMapMapper labelMapMapper;
    @Autowired
    private IssueContentMapper issueContentMapper;
    @Autowired
    private IssueOpLogMapper issueOpLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Issue save(IssueNewForm form, Long projectId) {
        Issue issue = new Issue();
        issue.setProjectId(projectId);
        issue.setTitle(form.getTitle());
        // 如果没有指定用户，则指定创建者本人
        if(form.getAssigneeId() != null) {
            issue.setAssigneeId(form.getAssigneeId());
        }else{
//            issue.setAssigneeId(currentUser.getId());
        }
        issue.setAuthorId(currentUser.getId());
        issue.setCreateTime(new Date());
        issue.setState(DB.IssueStateEnum.OPEN);
        if(form.getPriority() != null) {
            DB.IssuePriorityEnum priorityEnum = DB.IssuePriorityEnum.valueOf(form.getPriority());
            if(priorityEnum != null) {
                issue.setPriority(priorityEnum);
            }
        }

        issue.setEnabled(true);

        insert(issue);
        IssueContent issueContent = new IssueContent();
        issueContent.setContent(form.getContent());
        issueContent.setIssueId(issue.getId());
        issueContentMapper.insert(issueContent);

        List<String> labelNames = form.getLabelName();
        if(labelNames != null && !labelNames.isEmpty()) {
            labelNames.forEach( name -> {
                IssueLabel label = labelMapper.getByName(projectId, name);
                if(label != null) {
                    IssueLabelMap labelMap = new IssueLabelMap();
                    labelMap.setIssueId(issue.getId());
                    labelMap.setLabelId(label.getId());
                    labelMap.setEnabled(true);
                    labelMapMapper.insert(labelMap);
                    // 引用数量 +1
                    labelMapper.incr(label.getId());
                }else{
                    // label 不存在需要处理下
                }
            });
        }

        return issue;
    }

    public PageInfo<Issue> page(IssueListForm form, Long projectId) {
        IssueCondition query = getIssueCondition(form, projectId);

        if(StringUtils.isNotBlank(form.getState())) {
            DB.IssueStateEnum stateEnum = DB.IssueStateEnum.nameOf(form.getState());
            if(stateEnum == null) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在状态");
            }
            query.setState(stateEnum);
        }
        final List<Long> labelIds = getLabelIds(form, projectId);
        //分页处理
        PageInfo<Issue> page = PageHelper.startPage(form.getPageNo(), form.getPageSize())
                            .setOrderBy(form.getOrderBy() + " " + form.getOrder())
                            .doSelectPageInfo(()-> issueMapper.listByCondition(query, labelIds));

//        PageHelper.startPage(form.getPageNo(), form.getPageSize());
//        PageHelper.orderBy(form.getOrderBy() + " " + form.getOrder());
//        List<Issue> issues = issueMapper.listByCondition(query, labelIds);
        return page;
    }

    public List<IssueStateCountDTO> getCountGroupByState(IssueListForm form, Long projectId) {
        IssueCondition query = getIssueCondition(form, projectId);
        final List<Long> labelIds = getLabelIds(form, projectId);
        return issueMapper.getCountGroupByState(query, labelIds);
    }

    protected List<Long> getLabelIds(IssueListForm form, Long projectId) {
        final List<Long> labelIds = new ArrayList<>();
        if(StringUtils.isNotBlank(form.getLabelName())) {
            List<String> names = Arrays.asList(form.getLabelName().split(","));

            IssueLabelCondition condition = new IssueLabelCondition();
            condition.setProjectId(projectId);
            condition.setName(names);

            List<IssueLabel> labels = labelMapper.selectByCondition(condition);

            labelIds.addAll(labels.stream().map(IssueLabel::getId).collect(Collectors.toList()));

            if(labelIds.size() != names.size()) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在的标签");
            }
        }
        return labelIds;
    }

    protected IssueCondition getIssueCondition(IssueListForm form, Long projectId) {
        IssueCondition query = new IssueCondition();
        query.setProjectId(projectId);

        if(form.getAuthorId() != null) {
            query.setAuthorId(form.getAuthorId());
        }
        if(form.getAssigneeId() != null) {
            query.setAssigneeId(form.getAssigneeId());
        }
        if(StringUtils.isNotBlank(form.getIssueSearch())) {
            query.setSearch(form.getIssueSearch());
        }
        return query;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateState(Long issueId, DB.IssueStateEnum stateEnum) {
        Issue issue = get(issueId);
        if(issue != null) {
            issue.setState(stateEnum);
            issueMapper.update(issue);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(Long projectId, Long issueId, IssueUpdateForm form) {
        if(StringUtils.isNotBlank(form.getTitle())) {
            issueMapper.updateProperty(Issue::getTitle, form.getTitle(), issueId);
        }
        if(form.getAssigneeId() != null) {
            issueMapper.updateProperty(Issue::getAssigneeId, form.getAssigneeId(), issueId);
        }
        List<String> labelNames = form.getLabelName();
        if(labelNames != null && !labelNames.isEmpty()) {
            // 删除旧的标签配置
            labelMapMapper.deleteByIssueId(issueId);
            labelMapper.decrByProjectId(projectId);

            // 添加新的标签配置
            labelNames.forEach( name -> {
                IssueLabel label = labelMapper.getByName(projectId, name);
                IssueLabelMap labelMap = new IssueLabelMap();
                labelMap.setIssueId(issueId);
                labelMap.setLabelId(label.getId());
                labelMap.setEnabled(true);
                labelMapMapper.insert(labelMap);
                // 引用数量 +1
                labelMapper.incr(label.getId());
            });
        }
    }

    public IssueContent getContent(Long issueId) {
        return issueContentMapper.selectByProperty(IssueContent::getIssueId, issueId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateContent(Long issueId, String content) {
        IssueContent issueContent = getContent(issueId);
        issueContent.setContent(content);
        issueContentMapper.update(issueContent);
    }
}
