package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.exception.AuthorizedException;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.form.MembersQueryForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectMemberVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("项目成员相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project")
public class ProjectMemberController extends BaseController {

    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private UserService userService;
    @Autowired
    private User currentUser;

    @ApiOperation(value="添加项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/member/addAll")
    public void addMembers(@RequestBody @Valid MembersModifyForm form){
        projectValidator.validateProjectExist(form.getProjectId());
        userValidator.validateUsernamesExist(form.getUsernames());
        projectMemberService.addMembers(form);
    }

    @ApiOperation(value="删除项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/member/removeAll")
    public Result removeMembers(@RequestBody @Valid MembersModifyForm form) {

        Project project = projectValidator.validateProjectExist(form.getProjectId());
        List<User> members = userService.findByUsernames(form.getUsernames());
        Set<Long> memberIds = members.stream().map(User::getId).collect(Collectors.toSet());

        if(memberIds.contains(project.getCreateBy())) {
            User user = userService.findOne(project.getCreateBy());
            return Result.error(ResultCode.ILLEGAL_OP, "无法删除" + user.getUsername());
        }
        memberIds.forEach(memberId -> projectValidator.validateUserIsProjectMember(form.getProjectId(), memberId));
        projectMemberService.deleteMemberList(project, memberIds);
        return Result.success();
    }

    @ApiOperation(value="获得项目的成员列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/{projectId}/members")
    public List members(@PathVariable Long projectId) {
        projectValidator.validateProjectExist(projectId);
        List<User> users = projectMemberService.findAllMembers(projectId);
        return buildVOs(users, UserVO.class);
    }


    @ApiOperation(value="查询用户，区分是否在项目中", notes = "")
    @ResponseBody
    @GetMapping(value = "/member/query")
    public Map queryMember(@Valid MembersQueryForm form) {
        projectValidator.validateProjectExist(form.getProjectId());
        return projectMemberService.queryMembersAndFlagIsInProject(form.getProjectId(), form.getUsername(), form.getEmail(), form.getPhone());
    }


    @ApiOperation(value="获得当前用户在某个项目中的角色", notes = "")
    @ResponseBody
    @GetMapping(value = "/{projectId}/memberRole")
    public ProjectMemberVO memberRole(@PathVariable(value = "projectId") Long projectId) {
        projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());
        ProjectMember pm = projectMemberService.getProjectRole(projectId, currentUser.getId());
        if (null == pm){
            throw new AuthorizedException(ResultCode.ILLEGAL_OP, "该用户不属于此项目");
        }
        ProjectMemberVO vo = new ProjectMemberVO();
        BeanUtils.copyProperties(pm, vo);
        return vo;
    }

}
