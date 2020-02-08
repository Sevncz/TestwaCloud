package com.testwa.distest.server.web.issue.mgr;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.form.IssueUpdateForm;
import com.testwa.distest.server.service.issue.service.*;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.issue.vo.IssueCommentVO;
import com.testwa.distest.server.web.issue.vo.IssueLabelVO;
import com.testwa.distest.server.web.issue.vo.IssueStateCountVO;
import com.testwa.distest.server.web.issue.vo.IssueVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author wen
 * @create 2018-12-26 11:01
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class IssueMgr {

    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueAssigneeService issueAssigneeService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private UserService userService;
    @Autowired
    private IssueCommentService issueCommentService;
    @Autowired
    private IssueOperationLogService issueOperationLogService;
    @Autowired
    private User currentUser;
    @Autowired
    private RedisCacheManager redisCacheManager;


    public List<IssueVO> buildIssueVOList(List<Issue> issues) {
        return issues.stream().map(this::buildIssueVO).collect(Collectors.toList());
    }

    public IssueVO buildIssueVO(Issue issue) {
        IssueVO vo = new IssueVO();
        BeanUtils.copyProperties(issue, vo);

        // 获得 issue 的标签列表
        List<IssueLabel> issueLabels = labelService.listByIssueId(issue.getId());
        List<IssueLabelVO> lableVOs = issueLabels.stream().map(lable -> {
            IssueLabelVO labelVO = new IssueLabelVO();
            BeanUtils.copyProperties(lable, labelVO);
            return labelVO;
        }).collect(Collectors.toList());
        vo.setLabels(lableVOs);

        // 获得创建人
        UserVO authorVO = getUserVO(issue.getAuthorId());
        vo.setAuthor(authorVO);

        // 获得指派者
        List<IssueAssignee> issueAssignees = issueAssigneeService.getByIssueId(issue.getId());
        if(issueAssignees != null && !issueAssignees.isEmpty()) {
            issueAssignees.forEach( issueAssignee -> {
                UserVO assigneeVO = getUserVO(issueAssignee.getAssigneeId());
                vo.addAssignee(assigneeVO);
            });
        }

        return vo;
    }

    public UserVO getUserVO(Long userId) {
        User user = userService.get(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    public IssueStateCountVO getStateCountVO(IssueListForm form, Long projectId) {
        IssueStateCountVO vo = new IssueStateCountVO();
        vo.setRejected(0L);
        vo.setClosed(0L);
        vo.setOpen(0L);
        vo.setFixed(0L);
        List<IssueStateCountDTO> countDTOList = issueService.getCountGroupByState(form, projectId);
        countDTOList.forEach( dto -> {
            switch(dto.getIssueState()) {
                case OPEN:
                    vo.setOpen(dto.getCountValue());
                    break;
                case CLOSED:
                    vo.setClosed(dto.getCountValue());
                    break;
                case REJECT:
                    vo.setRejected(dto.getCountValue());
                    break;
                case FIXED:
                    vo.setFixed(dto.getCountValue());
                    break;
            }
        });

        return vo;
    }

    public List<IssueCommentVO> buildIssueCommentVOs(List<IssueComment> commentList) {

        if(!commentList.isEmpty()) {
            return commentList.stream().map(comment -> {
                IssueCommentVO vo = new IssueCommentVO();
                BeanUtils.copyProperties(comment, vo);
                UserVO assigneeVO = getUserVO(comment.getAuthorId());
                vo.setAuthor(assigneeVO);
                return vo;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Issue save(IssueNewForm form, Long projectId) {
        // 获得 issue_seq
        String seqKey = issueService.getIssueSeqRedisKey(projectId);
        Long seq = redisCacheManager.incr(seqKey);
        try{
            // 保存 issue
            Issue issue = issueService.save(projectId, form.getTitle(), form.getPriority(), DB.IssueStateEnum.OPEN, seq);

            // 保存 issue content
            issueService.saveIssueContent(issue.getId(), form.getContent(), form.getAttachments());

            // 保存 assignee
            if(form.getAssigneeIds() != null && !form.getAssigneeIds().isEmpty()) {
                form.getAssigneeIds().forEach(assigneeId -> issueAssigneeService.save(issue.getId(), assigneeId));
            }

            // 保存 label
            List<String> labelNames = form.getLabelName();
            if(labelNames != null && !labelNames.isEmpty()) {
                labelService.newLabelForIssue(projectId, form.getLabelName(), issue.getId());
            }
            return issue;
        }catch (BusinessException e) {
            // 异常时回滚
            redisCacheManager.decr(seqKey);
            throw new BusinessException(e.getCode(), e.getMessage());
        }catch (Exception e) {
            redisCacheManager.decr(seqKey);
            throw new BusinessException(ResultCode.SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void edit(Long issueId, IssueUpdateForm form) {
        // 更新 title
        if(StringUtils.isNotBlank(form.getTitle())) {
            issueService.updateTitle(issueId, form.getTitle());
        }
        // 更新 content
        if(StringUtils.isNotBlank(form.getContent())) {
            issueService.updateContent(issueId, form.getContent());
        }

    }

    /**
     * @Description: 获得所有参与者
     * @Param: [issueId]
     * @Return: java.util.List<com.testwa.distest.server.web.auth.vo.UserVO>
     * @Author wen
     * @Date 2019/1/8 18:53
     */
    public List<UserVO> listParticipantByIssue(Long issueId) {
        Issue issue = issueService.get(issueId);
        if(issue == null) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "issue 不可用");
        }
        // 创建者
        List<Long> allParticipantList = new ArrayList<>();
        allParticipantList.add(issue.getAuthorId());

        // 负责人
        List<IssueAssignee> issueAssignees = issueAssigneeService.getByIssueId(issueId);
        List<Long> issueAssigneeIds = issueAssignees.stream().map(IssueAssignee::getAssigneeId).collect(Collectors.toList());
        allParticipantList.addAll(issueAssigneeIds);

        // 评论者
        List<Long> commentUserIds = issueCommentService.listCommentUserId(issueId);
        allParticipantList.addAll(commentUserIds);

        // 修改者
        List<Long> opUserIds = issueOperationLogService.listOperationUserId(issueId);
        allParticipantList.addAll(opUserIds);

        return allParticipantList.stream().distinct().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * @Description: 删除评论
     * @Param: [issueId, commentId]
     * @Return: void
     * @Author wen
     * @Date 2019/1/11 14:24
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteComment(Long issueId, Long commentId) {
        issueService.decrCommentNum(issueId);
        issueCommentService.disable(commentId);
    }
}
