package com.testwa.distest.server.service.issue.service;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.async.IssueLogTask;
import com.testwa.distest.server.entity.IssueAssignee;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.IssueAssigneeMapper;
import com.testwa.distest.server.mapper.UserMapper;
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
public class IssueAssigneeService extends BaseService<IssueAssignee, Long> {

    @Autowired
    private IssueAssigneeMapper issueAssigneeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IssueLogTask issueLogTask;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueAssignee save(Long issueId, Long assigneeId) {
        User assigneeUser = userMapper.selectById(assigneeId);
        if(assigneeUser == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "负责人不存在");
        }
        IssueAssignee assignee = new IssueAssignee();
        assignee.setAssigneeId(assigneeId);
        assignee.setIssueId(issueId);
        assignee.setAssignRole(DB.IssueAssignRoleEnum.LEADER);
        issueAssigneeMapper.insert(assignee);
        // 保存操作日志
        issueLogTask.logAddForAssignee(issueId, currentUser.getId(), assigneeUser.getUsername());
        return assignee;
    }

    public List<IssueAssignee> getByIssueId(Long issueId) {
        return issueAssigneeMapper.selectListByProperty(IssueAssignee::getIssueId, issueId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAssignees(List<Long> assigneeIds, Long issueId) {
        issueAssigneeMapper.deleteByIssueId(issueId);
        if(assigneeIds != null && !assigneeIds.isEmpty()) {
            assigneeIds.forEach(assigneeId -> {
                save(issueId, assigneeId);
            });
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int delete(Long issueId, Long assigneeId) {
        User assigneeUser = userMapper.selectById(assigneeId);
        if(assigneeUser == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "负责人不存在");
        }
        int line = issueAssigneeMapper.deleteByIssueIdAndAssigneeId(issueId, assigneeId);
        // 保存操作日志
        issueLogTask.logRemoveForAssignee(issueId, currentUser.getId(), assigneeUser.getUsername());
        return line;
    }

    public IssueAssignee getByIssueIdAndAssigneeId(Long issueId, Long assigneeId) {

        return issueAssigneeMapper.getByIssueIdAndAssigneeId(issueId, assigneeId);
    }
}
