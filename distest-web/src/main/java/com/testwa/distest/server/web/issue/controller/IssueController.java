package com.testwa.distest.server.web.issue.controller;


import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.issue.validator.IssueValidator;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api("issue相关")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class IssueController {

    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueValidator issueValidator;
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
        issueService.save(form, projectId);
    }

    @ApiOperation(value="issue分页列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/issuePage")
    public PageInfo issuePage(@PathVariable Long projectId, @Valid IssueListForm form) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        if(form.getAssigneeId() != null) {
            userValidator.validateUserIdExist(form.getAssigneeId());
        }
        if(form.getAuthorId() != null) {
            userValidator.validateUserIdExist(form.getAuthorId());
        }
        return issueService.page(form, projectId);
    }

    @ApiOperation(value="关闭issue")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/close")
    public void issueClose(@PathVariable Long projectId, @PathVariable Long issueId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        if(DB.IssueStateEnum.CLOSED.equals(issue.getState()) ) {
            return;
        }
        log.info("Close issue {} BY {}", issueId, currentUser.getId());
        issueService.updateState(issueId, DB.IssueStateEnum.CLOSED);

    }

    @ApiOperation(value="拒绝issue")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/issue/{issueId}/reject")
    public void issueReject(@PathVariable Long projectId, @PathVariable Long issueId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        Issue issue = issueValidator.validateIssueExist(issueId);
        if(!DB.IssueStateEnum.OPEN.equals(issue.getState())) {
            return;
        }

        log.info("Reject issue {} BY {}", issueId, currentUser.getId());
        issueService.updateState(issueId, DB.IssueStateEnum.REJECT);

    }

}
