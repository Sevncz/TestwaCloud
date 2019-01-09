package com.testwa.distest.server.service.issue.service;

import com.github.pagehelper.PageHelper;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.IssueLabelDict;
import com.testwa.distest.server.entity.IssueOperationLog;
import com.testwa.distest.server.mapper.IssueLabelDictMapper;
import com.testwa.distest.server.mapper.IssueOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author wen
 * @create 2018-12-17 10:23
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class IssueOperationLogService extends BaseService<IssueOperationLog, Long> {

    @Autowired
    private IssueOperationLogMapper issueOperationLogMapper;


    public List<IssueOperationLog> listByIssueId(Long issueId) {
        PageHelper.orderBy("create_time desc");
        return issueOperationLogMapper.selectListByProperty(IssueOperationLog::getIssueId, issueId);
    }

    /**
     * @Description: 操作过这个 issue 的人员名单
     * @Param: [issueId]
     * @Return: java.util.List<java.lang.Long>
     * @Author wen
     * @Date 2019/1/8 19:18
     */
    public List<Long> listOperationUserId(Long issueId) {
        return issueOperationLogMapper.listOperationUserId(issueId);
    }
}
