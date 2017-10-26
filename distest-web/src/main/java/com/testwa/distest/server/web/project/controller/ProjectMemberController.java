package com.testwa.distest.server.web.project.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.server.mvc.entity.Project;
import com.testwa.distest.server.mvc.entity.ProjectMember;
import com.testwa.distest.server.mvc.entity.User;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectMemberVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by wen on 20/10/2017.
 */
@Api("项目成员相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project/member")
public class ProjectMemberController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ProjectMemberController.class);

    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private UserValidator userValidator;

    @ApiOperation(value="添加项目成员", notes = "")
    @ResponseBody
    @RequestMapping(value = "/add/all", method= RequestMethod.POST)
    public Result addMembers(@RequestBody @Valid MembersModifyForm form) throws AccountException, NoSuchProjectException, AccountAlreadyExistException, AuthorizedException, ParamsException, AccountNotFoundException {
        projectValidator.validateProjectExist(form.getProjectId());
        if(form.getUsernames() != null && form.getUsernames().size() > 0){
            userValidator.validateUsernames(form.getUsernames());
        }
        projectMemberService.addMembers(form);
        return ok();
    }

    @ApiOperation(value="删除项目成员", notes = "")
    @ResponseBody
    @RequestMapping(value = "/delete/all", method= RequestMethod.POST)
    public Result deleteMembers(@RequestBody @Valid MembersModifyForm form) throws NoSuchProjectException {

        projectValidator.validateProjectExist(form.getProjectId());
        projectMemberService.delMembers(form);
        return ok();
    }

    @ApiOperation(value="获得项目的成员列表", notes = "")
    @ResponseBody
    @RequestMapping(value = "/{projectId}", method= RequestMethod.GET)
    public Result members(@PathVariable Long projectId) throws NoSuchProjectException {
        List<User> users = projectMemberService.findAllMembers(projectId);
        List<UserVO> vo = buildVOs(users, UserVO.class);
        return ok(vo);
    }


    @ApiOperation(value="查询用户，区分是否在项目中", notes = "")
    @ResponseBody
    @RequestMapping(value = "/query", method= RequestMethod.GET)
    public Result queryMember(@RequestParam(value = "projectId")Long projectId,
                              @RequestParam(value = "memberName")String memberName,
                              @RequestParam(value = "email")String email,
                              @RequestParam(value = "phone")String phone) throws NoSuchProjectException {
        projectValidator.validateProjectExist(projectId);
        Map<String, List<UserVO>> result = projectMemberService.findMembers(projectId, memberName, email, phone);
        return ok(result);
    }


    @ApiOperation(value="获得当前用户在某个项目中的角色", notes = "")
    @ResponseBody
    @RequestMapping(value = "/role", method= RequestMethod.GET)
    public Result projectRole(@RequestParam(value = "projectId")Long projectId) throws NoSuchProjectException, AccountException, DBException, AuthorizedException {
        ProjectMember pm = projectMemberService.getProjectRole(projectId);
        if (null == pm){
            throw new AuthorizedException("该用户不属于此项目");
        }
        ProjectMemberVO vo = new ProjectMemberVO();
        BeanUtils.copyProperties(pm, vo);
        return ok(vo);
    }

}
