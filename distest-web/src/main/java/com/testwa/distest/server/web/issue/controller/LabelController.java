package com.testwa.distest.server.web.issue.controller;


import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.issue.form.IssueLabelNewForm;
import com.testwa.distest.server.service.issue.form.IssueLabelUpdateForm;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.service.issue.service.LabelService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.issue.validator.IssueValidator;
import com.testwa.distest.server.web.issue.validator.LabelValidator;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api("label相关")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class LabelController {

    @Autowired
    private LabelService labelService;
    @Autowired
    private LabelValidator labelValidator;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private User currentUser;

    @ApiOperation(value="新增标签")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/labelNew")
    public void labelNew(@PathVariable Long projectId, @RequestBody @Valid IssueLabelNewForm form) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());

        labelValidator.validateLabelNameExist(projectId, form.getName());

        labelService.save(form, projectId);

    }

    @ApiOperation(value="更新标签")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/labelUpdate")
    public void labelUpdate(@PathVariable Long projectId, @RequestBody @Valid IssueLabelUpdateForm form) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());

        labelValidator.validateLabelNameExist(projectId, form.getName());

        labelService.update(form);

    }

    @ApiOperation(value="删除标签")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/label/{labelId}/delete")
    public void labelDelete(@PathVariable Long projectId, @RequestBody @Valid Long labelId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        labelValidator.validateLabelExist(labelId);

        labelService.delete(labelId);
    }

    @ApiOperation(value="标签列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/labelList")
    public void labelList(@PathVariable Long projectId) {

        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());

        labelService.list(projectId);

    }

}