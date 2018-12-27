package com.testwa.distest.server.service.issue.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.bo.BaseCondition;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.form.RequestListBase;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.IssueCommentCondition;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.condition.IssueLabelCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.*;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import com.testwa.distest.server.service.issue.form.CommentListForm;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.form.IssueUpdateForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
public class IssueCommentService extends BaseService<IssueComment, Long> {

    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private IssueCommentMapper issueCommentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssueComment save(Long issueId, String content) {
        IssueComment comment = new IssueComment();
        comment.setAuthorId(currentUser.getId());
        comment.setIssueId(issueId);
        comment.setContent(content);
        issueCommentMapper.insert(comment);
        return comment;
    }

    public PageInfo page(Long issueId, CommentListForm form) {
        IssueCommentCondition condition = new IssueCommentCondition();
        condition.setIssueId(issueId);
        PageInfo<Issue> page = PageHelper.startPage(form.getPageNo(), form.getPageSize())
                .setOrderBy(form.getOrderBy() + " " + form.getOrder())
                .doSelectPageInfo(()-> issueCommentMapper.selectByCondition(condition));
        return page;
    }

}
