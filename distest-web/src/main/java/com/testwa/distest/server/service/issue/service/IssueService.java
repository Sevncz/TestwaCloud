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
import com.testwa.distest.server.mapper.IssueLabelMapMapper;
import com.testwa.distest.server.mapper.IssueLabelMapper;
import com.testwa.distest.server.mapper.IssueMapper;
import com.testwa.distest.server.mapper.UserMapper;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.web.issue.vo.IssueLabelVO;
import com.testwa.distest.server.web.issue.vo.IssueVO;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    private UserMapper userMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRED)
    public Issue save(IssueNewForm form, Long projectId) {
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
        issue.setAuthorId(currentUser.getId());
        issue.setCreateTime(new Date());
        issue.setState(DB.IssueStateEnum.OPEN);

        issue.setEnabled(true);

        insert(issue);

        List<String> labelNames = form.getLabelName();
        if(labelNames != null && !labelNames.isEmpty()) {
            labelNames.forEach( name -> {
                IssueLabel label = labelMapper.getByName(projectId, name);
                IssueLabelMap labelMap = new IssueLabelMap();
                labelMap.setIssueId(issue.getId());
                labelMap.setLabelId(label.getId());
                labelMap.setEnabled(true);
                labelMapMapper.insert(labelMap);
                // 引用数量 +1
                labelMapper.addNum(label.getId());
            });
        }
        return issue;
    }

    public PageInfo<IssueVO> page(IssueListForm form, Long projectId) {
        IssueCondition query = new IssueCondition();
        query.setProjectId(projectId);

        if(form.getAuthorId() != null) {
            query.setAuthorId(form.getAuthorId());
        }
        if(form.getAssigneeId() != null) {
            query.setAssigneeId(form.getAssigneeId());
        }
        List<Long> labelIds = null;
        if(StringUtils.isNotBlank(form.getLabelName())) {
            List<String> names = Arrays.asList(form.getLabelName().split(","));

            IssueLabelCondition condition = new IssueLabelCondition();
            condition.setProjectId(projectId);
            condition.setName(names);

            List<IssueLabel> labels = labelMapper.selectByCondition(condition);

            labelIds = labels.stream().map(IssueLabel::getId).collect(Collectors.toList());

            if(labelIds.size() != names.size()) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在的标签");
            }
        }
        if(StringUtils.isNotBlank(form.getState())) {
            DB.IssueStateEnum stateEnum = DB.IssueStateEnum.nameOf(form.getState());
            if(stateEnum == null) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "不存在状态");
            }
            query.setState(stateEnum);
        }
        if(StringUtils.isNotBlank(form.getIssueSearch())) {
            query.setSearch(form.getIssueSearch());
        }
        //分页处理
        PageHelper.startPage(form.getPageNo(), form.getPageSize());
        PageHelper.orderBy(form.getOrderBy() + " " + form.getOrder());
        List<Issue> issues = issueMapper.listByCondition(query, labelIds);
        List<IssueVO> issueVOS = buildIssueVOList(issues);
        return new PageInfo(issueVOS);
    }

    public List<IssueVO> buildIssueVOList(List<Issue> issues) {
        return issues.stream().map(this::buildIssueVO).collect(Collectors.toList());
    }

    public IssueVO buildIssueVO(Issue issue) {
        IssueVO vo = new IssueVO();
        BeanUtils.copyProperties(issue, vo);

        // 获得 issue 的标签列表
        List<IssueLabel> issueLabels = labelMapper.listByIssueId(issue.getId());
        List<IssueLabelVO> lableVOs = issueLabels.stream().map(lable -> {
            IssueLabelVO labelVO = new IssueLabelVO();
            BeanUtils.copyProperties(lable, labelVO);
            return labelVO;
        }).collect(Collectors.toList());
        vo.setLabels(lableVOs);

        // 获得创建人
        User author = userMapper.selectById(issue.getAuthorId());
        UserVO authorVO = new UserVO();
        BeanUtils.copyProperties(author, authorVO);
        vo.setAuthor(authorVO);
        // 获得指派者
        if(issue.getAssigneeId() != null) {
            User assignee = userMapper.selectById(issue.getAssigneeId());
            UserVO assigneeVO = new UserVO();
            BeanUtils.copyProperties(assignee, assigneeVO);
            vo.setAssignee(assigneeVO);
        }

        return vo;
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
