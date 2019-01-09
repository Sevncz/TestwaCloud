package com.testwa.distest.server.web.issue.controller;


import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.issue.form.*;
import com.testwa.distest.server.service.issue.service.*;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.issue.mgr.IssueMgr;
import com.testwa.distest.server.web.issue.validator.IssueAssigneeValidator;
import com.testwa.distest.server.web.issue.validator.IssueValidator;
import com.testwa.distest.server.web.issue.validator.LabelMapValidator;
import com.testwa.distest.server.web.issue.validator.LabelValidator;
import com.testwa.distest.server.web.issue.vo.*;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Api("issue相关")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class IssueController {

    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueCommentService issueCommentService;
    @Autowired
    private IssueAssigneeService issueAssigneeService;
    @Autowired
    private LabelMapService labelMapService;
    @Autowired
    private IssueOperationLogService issueOperationLogService;
    @Autowired
    private IssueMgr issueMgr;
    @Autowired
    private IssueValidator issueValidator;
    @Autowired
    private LabelValidator labelValidator;
    @Autowired
    private LabelMapValidator labelMapValidator;
    @Autowired
    private IssueAssigneeValidator issueAssigneeValidator;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value="创建issue")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issueNew")
    public Long issueSave(@PathVariable Long projectId, @RequestBody @Valid IssueNewForm form) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        if(form.getAssigneeIds() != null && !form.getAssigneeIds().isEmpty()) {
            form.getAssigneeIds().forEach( assigneeId -> userValidator.validateUserIdExist(assigneeId));
        }
        if(form.getPriority() != null) {
            DB.IssuePriorityEnum priorityEnum = DB.IssuePriorityEnum.valueOf(form.getPriority());
            if(priorityEnum == null) {
                throw new BusinessException(ResultCode.ILLEGAL_PARAM, "issue 优先级错误");
            }
        }
        if(form.getLabelName() != null) {
            form.getLabelName().forEach( name -> {
                labelValidator.validateLabelNameExist(projectId, name);
            });
        }
        Issue issue = issueMgr.save(form, projectId);
        return issue.getId();
    }

    @ApiOperation(value="issue 分页列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issuePage")
    public IssuePageVO issuePage(@PathVariable Long projectId, @Valid IssueListForm form) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        if(form.getAssigneeId() != null) {
            userValidator.validateUserIdExist(form.getAssigneeId());
        }
        if(form.getAuthorId() != null) {
            userValidator.validateUserIdExist(form.getAuthorId());
        }
        if(StringUtils.isNotBlank(form.getLabelName())) {
            List<String> names = Arrays.asList(form.getLabelName().split(","));
            names.forEach( name -> {
                labelValidator.validateLabelNameExist(projectId, name.trim());
            });
        }
        PageInfo issuePageInfo = issueService.page(form, projectId);
        List<IssueVO> issueVOS = issueMgr.buildIssueVOList(issuePageInfo.getList());
        issuePageInfo.setList(issueVOS);

        IssueStateCountVO stateCountVO = issueMgr.getStateCountVO(form, projectId);

        IssuePageVO pageVO = new IssuePageVO();
        pageVO.setPageInfo(issuePageInfo);
        pageVO.setStateInfo(stateCountVO);
        return pageVO;
    }

    @ApiOperation(value="issue 状态统计")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issue/stateCount")
    public IssueStateCountVO issueStateCount(@PathVariable Long projectId, @Valid IssueListForm form) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        if(form.getAssigneeId() != null) {
            userValidator.validateUserIdExist(form.getAssigneeId());
        }
        if(form.getAuthorId() != null) {
            userValidator.validateUserIdExist(form.getAuthorId());
        }
        if(StringUtils.isNotBlank(form.getLabelName())) {
            List<String> names = Arrays.asList(form.getLabelName().split(","));
            names.forEach( name -> {
                labelValidator.validateLabelNameExist(projectId, name.trim());
            });
        }

        return issueMgr.getStateCountVO(form, projectId);

    }

    @ApiOperation(value="issue详情")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issue/{issueId}/detail")
    public IssueDetailVO issueDetail(@PathVariable Long projectId, @PathVariable Long issueId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);

        IssueContent content = issueService.getContent(issue.getId());

        IssueVO vo = issueMgr.buildIssueVO(issue);
        List<UserVO> participant = issueMgr.listParticipantByIssue(issueId);
        IssueDetailVO detailVO = new IssueDetailVO();
        BeanUtils.copyProperties(vo, detailVO);
        if(content != null) {
            detailVO.setContent(content.getContent());
        }else{
            detailVO.setContent("");
        }
        detailVO.setParticipants(participant);
        return detailVO;
    }

    @ApiOperation(value="issue详情编辑")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/contentUpdate")
    public void issueContentUpdate(@PathVariable Long projectId, @PathVariable Long issueId, @RequestBody String content) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        issueValidator.validateIssueExist(issueId);

        issueService.updateContent(issueId, content);
    }

    @ApiOperation(value="issue 更新")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/update")
    public void issueUpdate(@PathVariable Long projectId, @PathVariable Long issueId, @RequestBody @Valid IssueUpdateForm form) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        issueValidator.validateIssueExist(issueId);

        if(form.getLabelName() != null) {
            form.getLabelName().forEach( name -> {
                labelValidator.validateLabelNameExist(projectId, name);
            });
        }
        issueMgr.update(projectId, issueId, form);
    }

    @ApiOperation(value="issue 添加 assignee")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/assigneeAdd/{assigneeId}")
    public void assigneeAdd(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Long assigneeId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        projectValidator.validateUserIsProjectMember(projectId, assigneeId);
        issueValidator.validateIssueExist(issueId);
        issueAssigneeValidator.validateAssigneeNotInIssue(issueId, assigneeId);

        issueAssigneeService.save(issueId, assigneeId);
    }

    @ApiOperation(value="issue 删除 assignee")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/assigneeRemove/{assigneeId}")
    public void assigneeRemove(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Long assigneeId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        issueValidator.validateIssueExist(issueId);
        issueAssigneeValidator.validateAssigneeInIssue(issueId, assigneeId);

        issueAssigneeService.delete(issueId, assigneeId);
    }

    @ApiOperation(value="issue 添加 label")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/labelAdd/{labelId}")
    public void labelAdd(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Long labelId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        IssueLabel issueLabel = labelValidator.validateLabelExist(labelId);
        if(!projectId.equals(issueLabel.getProjectId())) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "Label " + issueLabel.getName() + " 不属于该项目");
        }
        labelMapValidator.validateLabelNotInIssue(issueId, labelId);

        labelMapService.save(issueId, labelId);
    }

    @ApiOperation(value="issue 删除 label")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/labelRemove/{labelId}")
    public void labelRemove(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Long labelId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        issueValidator.validateIssueExist(issueId);
        labelMapValidator.validateLabelInIssue(issueId, labelId);

        labelMapService.delete(issueId, labelId);
    }

    @ApiOperation(value="更新issue状态")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/stateChange/{state}")
    public void stateChange(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Integer state) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        log.info("Update issue {} BY {}", issueId, currentUser.getId());
        issueService.updateState(issueId, state);
    }

    @ApiOperation(value="更新 issue 优先级")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/priorityChange/{priority}")
    public void priorityChange(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable Integer priority) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        log.info("Update issue {} BY {}", issueId, currentUser.getId());
        issueService.updatePriority(issueId, priority);
    }

    @ApiOperation(value="issue 评论列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issue/{issueId}/commentPage")
    public PageInfo<IssueCommentVO> commentPage(@PathVariable Long projectId, @PathVariable Long issueId, @Valid CommentListForm form) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        PageInfo issueComments = issueCommentService.page(issueId, form);
        List<IssueCommentVO> vos = issueMgr.buildIssueCommentVOs(issueComments.getList());
        issueComments.setList(vos);
        return issueComments;
    }

    @ApiOperation(value="创建issue comment")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/commentNew")
    public void issueCommentSave(@PathVariable Long projectId, @PathVariable Long issueId, @RequestBody @Valid IssueCommentNewForm form) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        issueCommentService.save(issueId, form.getContent());
    }


    @ApiOperation(value="删除issue ")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/delete")
    public void issueDelete(@PathVariable Long projectId, @PathVariable Long issueId) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        if(!currentUser.getId().equals(issue.getAuthorId())) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "您无法删除 issue");
        }
        issueService.disable(issueId);
    }


    @ApiOperation(value="issue 操作历史分页列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issue/{issueId}/operationLogs")
    public List operationLogList(@PathVariable Long projectId, @PathVariable Long issueId) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);

        List<IssueOperationLog> operationLogs = issueOperationLogService.listByIssueId(issueId);

        List<IssueOperationLogVO> issueOperationLogVOS = new ArrayList<>();
        if(operationLogs != null && !operationLogs.isEmpty()) {
            operationLogs.forEach( opLog -> {
                IssueOperationLogVO vo = new IssueOperationLogVO();
                BeanUtils.copyProperties(opLog, vo);
                UserVO userVO = issueMgr.getUserVO(opLog.getUserId());
                vo.setUser(userVO);
                issueOperationLogVOS.add(vo);
            });
        }

        return issueOperationLogVOS;
    }

}
