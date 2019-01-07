package com.testwa.distest.server.service.issue.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.IssueAssignee;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.IssueAssigneeMapper;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueAssignee save(Long issueId, Long assigneeId) {
        IssueAssignee assignee = new IssueAssignee();
        assignee.setAssigneeId(assigneeId);
        assignee.setIssueId(issueId);
        assignee.setAssignRole(DB.IssueAssignRoleEnum.LEADER);
        assignee.setEnabled(true);
        issueAssigneeMapper.insert(assignee);
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
}
