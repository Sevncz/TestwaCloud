package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.ResultVO;
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
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("项目成员相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project/member")
public class ProjectMemberController extends BaseController {

    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private UserService userService;

    @ApiOperation(value="添加项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/add/all")
    public ResultVO addMembers(@RequestBody @Valid MembersModifyForm form) throws AccountNotFoundException, ObjectNotExistsException, AuthorizedException, ParamsException {
        projectValidator.validateProjectExist(form.getProjectId());
        if(form.getUsernames() != null && form.getUsernames().size() > 0){
            userValidator.validateUsernamesExist(form.getUsernames());
        }
        projectMemberService.addMembers(form);
        return ok();
    }

    @ApiOperation(value="删除项目成员", notes = "")
    @ResponseBody
    @PostMapping(value = "/remove/all")
    public ResultVO removeMembers(@RequestBody @Valid MembersModifyForm form) throws ObjectNotExistsException, ParamsIsNullException {

        if(form.getUsernames() == null && form.getUsernames().size() == 0){
            throw new ParamsIsNullException("参数不能为空");
        }
        projectValidator.validateProjectExist(form.getProjectId());

        projectMemberService.delMembers(form);
        return ok();
    }

    @ApiOperation(value="获得项目的成员列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/{projectId}")
    public ResultVO members(@PathVariable Long projectId) throws ObjectNotExistsException {
        projectValidator.validateProjectExist(projectId);

        List<User> users = projectMemberService.findAllMembers(projectId);
        List<UserVO> vo = buildVOs(users, UserVO.class);
        return ok(vo);
    }


    @ApiOperation(value="查询用户，区分是否在项目中", notes = "")
    @ResponseBody
    @GetMapping(value = "/query")
    public ResultVO queryMember(MembersQueryForm form) throws ObjectNotExistsException {
        projectValidator.validateProjectExist(form.getProjectId());
        Map<String, List<UserVO>> result = projectMemberService.queryMembersAndFlagIsInProject(form.getProjectId(), form.getUsername(), form.getEmail(), form.getPhone());
        return ok(result);
    }


    @ApiOperation(value="获得当前用户在某个项目中的角色", notes = "")
    @ResponseBody
    @GetMapping(value = "/role")
    public ResultVO projectRole(@RequestParam(value = "projectId")Long projectId) throws AccountException, DBException, AuthorizedException, ObjectNotExistsException {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());
        ProjectMember pm = projectMemberService.getProjectRole(projectId, user.getId());
        if (null == pm){
            throw new AuthorizedException("该用户不属于此项目");
        }
        ProjectMemberVO vo = new ProjectMemberVO();
        BeanUtils.copyProperties(pm, vo);
        return ok(vo);
    }

}
