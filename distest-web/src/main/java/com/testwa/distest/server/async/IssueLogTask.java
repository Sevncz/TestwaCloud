package com.testwa.distest.server.async;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.IssueOperationLog;
import com.testwa.distest.server.service.issue.service.IssueOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author wen
 * @create 2019-01-08 15:45
 */

@Component
public class IssueLogTask {
    private static final String LOG_UPDATE_DESC = "将<b>%s</b>从%s的修改为<b>%s</b>";
    private static final String LOG_REMOVE_DESC = "移除<b>%s</b><b>%s</b>";
    private static final String LOG_ADD_DESC = "添加<b>%s</b><b>%s</b>";
    private static final String LOG_NEW_DESC = "创建了<b>%s</b>";

    private static final String LOG_OP_LABEL = "标签";
    private static final String LOG_OP_ASSIGNEE = "负责人";
    private static final String LOG_OP_STATE = "状态";
    private static final String LOG_OP_PRIORITY = "优先级";
    private static final String LOG_OP_ISSUE = "issue";

    @Autowired
    private IssueOperationLogService issueOperationLogService;

    @Async
    public void logUpdateForState(Long issueId, Long userId, String oldState, String newState) {
        update(issueId, userId, oldState, newState, LOG_OP_STATE);
    }

    @Async
    public void logUpdateForPriority(Long issueId, Long userId, String oldvalue, String newvalue) {
        update(issueId, userId, oldvalue, newvalue, LOG_OP_PRIORITY);
    }

    @Async
    public void logAddForAssignee(Long issueId, Long userId, String assignee) {
        add(issueId, userId, assignee, LOG_OP_ASSIGNEE);
    }

    @Async
    public void logRemoveForAssignee(Long issueId, Long userId, String assignee) {
        remove(issueId, userId, assignee, LOG_OP_ASSIGNEE);
    }

    @Async
    public void logAddForLabel(Long issueId, Long userId, String label) {
        add(issueId, userId, label, LOG_OP_LABEL);
    }

    @Async
    public void logNewIssue(Long issueId, Long userId) {
        IssueOperationLog operationLog = new IssueOperationLog();
        operationLog.setIssueId(issueId);
        operationLog.setOpType(DB.IssueOpTypeEnum.ADD);
        operationLog.setUserId(userId);
        operationLog.setContent(String.format(LOG_NEW_DESC, LOG_OP_ISSUE));
        issueOperationLogService.insert(operationLog);
    }

    @Async
    public void logRemoveForLabel(Long issueId, Long userId, String label) {
        remove(issueId, userId, label, LOG_OP_LABEL);
    }

    private void add(Long issueId, Long userId, String label, String logOpObject) {
        IssueOperationLog operationLog = new IssueOperationLog();
        operationLog.setIssueId(issueId);
        operationLog.setOpType(DB.IssueOpTypeEnum.ADD);
        operationLog.setUserId(userId);
        operationLog.setContent(String.format(LOG_ADD_DESC, logOpObject, label));
        issueOperationLogService.insert(operationLog);
    }

    private void remove(Long issueId, Long userId, String label, String logOpObject) {
        IssueOperationLog operationLog = new IssueOperationLog();
        operationLog.setIssueId(issueId);
        operationLog.setOpType(DB.IssueOpTypeEnum.REMOVE);
        operationLog.setUserId(userId);
        operationLog.setContent(String.format(LOG_REMOVE_DESC, logOpObject, label));
        issueOperationLogService.insert(operationLog);
    }

    private void update(Long issueId, Long userId, String oldvalue, String newvalue, String logOpObject) {
        IssueOperationLog operationLog = new IssueOperationLog();
        operationLog.setIssueId(issueId);
        operationLog.setOpType(DB.IssueOpTypeEnum.UPDATE);
        operationLog.setUserId(userId);
        operationLog.setContent(String.format(LOG_UPDATE_DESC, logOpObject, oldvalue, newvalue));
        issueOperationLogService.insert(operationLog);
    }
}
