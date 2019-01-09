package com.testwa.distest.server.service.issue.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.async.IssueLogTask;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.condition.IssueLabelCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.*;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import com.testwa.distest.server.service.issue.form.IssueListForm;
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
    private IssueContentMapper issueContentMapper;
    @Autowired
    private User currentUser;
    @Autowired
    private IssueLogTask issueLogTask;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Issue save(Long projectId, String title, Integer priorityValue, DB.IssueStateEnum stateEnum) {
        Issue issue = new Issue();
        issue.setProjectId(projectId);
        issue.setTitle(title);
        issue.setAuthorId(currentUser.getId());
        issue.setCreateTime(new Date());
        issue.setState(stateEnum);
        if(priorityValue != null) {
            DB.IssuePriorityEnum priorityEnum = DB.IssuePriorityEnum.valueOf(priorityValue);
            if(priorityEnum != null) {
                issue.setPriority(priorityEnum);
            }
        }
        issueMapper.insert(issue);

        issueLogTask.logNewIssue(issue.getId(), currentUser.getId());
        return issue;
    }

    public PageInfo<Issue> page(IssueListForm form, Long projectId) {
        IssueCondition query = getIssueCondition(form, projectId);
        PageInfo<Issue> page = new PageInfo<>();
        if(StringUtils.isNotBlank(form.getState())) {
            DB.IssueStateEnum stateEnum = DB.IssueStateEnum.nameOf(form.getState());
            if(stateEnum == null) {
                return page;
            }
            query.setState(stateEnum);
        }
        final List<Long> labelIds = getLabelIds(form, projectId);
        //分页处理
        page = PageHelper.startPage(form.getPageNo(), form.getPageSize())
                            .setOrderBy(form.getOrderBy() + " " + form.getOrder())
                            .doSelectPageInfo(()-> issueMapper.listByCondition(query, labelIds));
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateState(Long issueId, Integer state) {
        DB.IssueStateEnum newIssueStateEnum = DB.IssueStateEnum.valueOf(state);
        if(newIssueStateEnum == null) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "issue 状态不存在");
        }
        Issue oldIssue = get(issueId);
        int line =  issueMapper.updateProperty(Issue::getState, newIssueStateEnum, issueId);

        issueLogTask.logUpdateForState(issueId, currentUser.getId(), oldIssue.getState().getDesc(), newIssueStateEnum.getDesc());
        return line;
    }

    public IssueContent getContent(Long issueId) {
        return issueContentMapper.selectByProperty(IssueContent::getIssueId, issueId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateContent(Long issueId, String content) {
        IssueContent issueContent = getContent(issueId);
        issueContent.setContent(content);
        issueContentMapper.update(issueContent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTitle(String title, Long issueId) {
        issueMapper.updateProperty(Issue::getTitle, title, issueId);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIssueContent(Long issueId, String content) {
        IssueContent issueContent = new IssueContent();
        issueContent.setContent(content);
        issueContent.setIssueId(issueId);
        issueContentMapper.insert(issueContent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePriority(Long issueId, Integer issuePriority) {
        DB.IssuePriorityEnum priorityEnum = DB.IssuePriorityEnum.valueOf(issuePriority);
        if(priorityEnum == null) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "issue 优先级不存在");
        }
        Issue oldIssue = get(issueId);
        issueMapper.updateProperty(Issue::getPriority, priorityEnum, issueId);
        issueLogTask.logUpdateForPriority(issueId, currentUser.getId(), oldIssue.getPriority().getDesc(), priorityEnum.getDesc());
    }
}
