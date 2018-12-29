package com.testwa.distest.server.web.issue.controller;


import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueComment;
import com.testwa.distest.server.entity.IssueContent;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.issue.form.*;
import com.testwa.distest.server.service.issue.service.IssueCommentService;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.issue.mgr.IssueMgr;
import com.testwa.distest.server.web.issue.validator.IssueValidator;
import com.testwa.distest.server.web.issue.validator.LabelValidator;
import com.testwa.distest.server.web.issue.vo.*;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private IssueMgr issueMgr;
    @Autowired
    private IssueValidator issueValidator;
    @Autowired
    private LabelValidator labelValidator;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value="创建issue")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issueNew")
    public void issueSave(@PathVariable Long projectId, @RequestBody @Valid IssueNewForm form) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        if(form.getAssigneeId() != null) {
            userValidator.validateUserIdExist(form.getAssigneeId());
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
        issueService.save(form, projectId);
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

    @ApiOperation(value="更新issue状态")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/state/{state}")
    public void issueClose(@PathVariable Long projectId, @PathVariable Long issueId, @PathVariable int state) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        log.info("Close issue {} BY {}", issueId, currentUser.getId());
        DB.IssueStateEnum issueStateEnum = DB.IssueStateEnum.valueOf(state);
        if(issueStateEnum == null) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, "issue 状态不存在");
        }
        issueService.updateState(issueId, issueStateEnum);

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
        IssueDetailVO detailVO = new IssueDetailVO();
        BeanUtils.copyProperties(vo, detailVO);
        if(content != null) {
            detailVO.setContent(content.getContent());
        }else{
            detailVO.setContent("");
        }
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

    @ApiOperation(value="issue更新")
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

        issueService.update(projectId, issueId, form);

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
        if(!DB.IssueStateEnum.OPEN.equals(issue.getState())) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "Issue 已经" + issue.getState().getDesc());
        }
        issueCommentService.save(issueId, form.getContent());
    }

}
