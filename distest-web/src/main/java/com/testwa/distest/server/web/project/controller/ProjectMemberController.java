package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectMemberVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;


/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Api("项目成员相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project/member")
public class ProjectMemberController extends BaseController {

    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private UserValidator userValidator;

    @ApiOperation(value="添加项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/add/all")
    public Result addMembers(@RequestBody @Valid MembersModifyForm form) throws AccountNotFoundException, ObjectNotExistsException, AuthorizedException, ParamsException {
        projectValidator.validateProjectExist(form.getProjectId());
        if(form.getUsernames() != null && form.getUsernames().size() > 0){
            userValidator.validateUsernamesExist(form.getUsernames());
        }
        projectMemberService.addMembers(form);
        return ok();
    }

    @ApiOperation(value="删除项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/delete/all")
    public Result deleteMembers(@RequestBody @Valid MembersModifyForm form) throws ObjectNotExistsException {

        projectValidator.validateProjectExist(form.getProjectId());
        projectMemberService.delMembers(form);
        return ok();
    }

    @ApiOperation(value="获得项目的成员列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/{projectId}")
    public Result members(@PathVariable Long projectId) {
        List<User> users = projectMemberService.findAllMembers(projectId);
        List<UserVO> vo = buildVOs(users, UserVO.class);
        return ok(vo);
    }


    @ApiOperation(value="查询用户，区分是否在项目中", notes = "")
    @ResponseBody
    @GetMapping(value = "/query")
    public Result queryMember(@RequestParam(value = "projectId")Long projectId,
                              @RequestParam(value = "memberName")String memberName,
                              @RequestParam(value = "email")String email,
                              @RequestParam(value = "phone")String phone) throws ObjectNotExistsException {
        projectValidator.validateProjectExist(projectId);
        Map<String, List<UserVO>> result = projectMemberService.findMembers(projectId, memberName, email, phone);
        return ok(result);
    }


    @ApiOperation(value="获得当前用户在某个项目中的角色", notes = "")
    @ResponseBody
    @GetMapping(value = "/role")
    public Result projectRole(@RequestParam(value = "projectId")Long projectId) throws AccountException, DBException, AuthorizedException {
        ProjectMember pm = projectMemberService.getProjectRole(projectId);
        if (null == pm){
            throw new AuthorizedException("该用户不属于此项目");
        }
        ProjectMemberVO vo = new ProjectMemberVO();
        BeanUtils.copyProperties(pm, vo);
        return ok(vo);
    }

}
