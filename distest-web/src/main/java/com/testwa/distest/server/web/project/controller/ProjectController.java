package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.form.IDForm;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.exception.AuthorizedException;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.project.service.ViewMgr;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectDetailVO;
import com.testwa.distest.server.web.project.vo.ProjectStatis;
import com.testwa.distest.server.web.project.vo.ProjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("项目操作相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private ViewMgr viewMgr;
    @Autowired
    private User currentUser;

    @ApiOperation(value="创建项目")
    @ResponseBody
    @PostMapping(value = "/save")
    public ProjectVO save(@RequestBody @Valid ProjectNewForm form) {

        if(form.getMembers() != null && !form.getMembers().isEmpty()){
            userValidator.validateUsernamesExist(form.getMembers());
        }
        Project project = projectService.save(form, currentUser);

        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(currentUser, UserVO.class);
        vo.setCreateUser(userVO);
        return vo;
    }

    @ApiOperation(value="更新项目")
    @ResponseBody
    @PostMapping(value = "/update")
    public ProjectVO update(@RequestBody @Valid ProjectUpdateForm form) {
        projectValidator.validateProjectExist(form.getProjectId());

        checkProjectAdmin(form.getProjectId());

        if(form.getMembers() != null){
            userValidator.validateUsernamesExist(form.getMembers());
        }
        Project project = projectService.update(form);
        User createUser = userService.findOne(project.getCreateBy());

        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(createUser, UserVO.class);
        vo.setCreateUser(userVO);

        return vo;
    }


    @ApiOperation(value="删除多个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/deleteAll")
    public void deleteAll(@RequestBody @Valid IDListForm form) {
        form.getEntityIds().forEach(this::checkProjectOwner);
        projectService.delete(form.getEntityIds());
    }


    @ApiOperation(value="删除一个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/deleteOne")
    public void deleteOne(@RequestBody @Valid IDForm form) {
        checkProjectOwner(form.getEntityId());
        projectService.delete(form.getEntityId());
    }

    @ApiOperation(value="获取我的项目和我参加的项目列表，分页")
    @ResponseBody
    @GetMapping(value = "/page")
    public PageResult page(@Valid ProjectListForm form){
        PageResult<Project> projectPR = projectService.findAllByUserPage(form, currentUser);
        List<Project> projects = projectPR.getPages();
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projects) {
            User createUser = userService.findOne(p.getCreateBy());
            ProjectStatis ps = projectService.statis(p.getId());
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(createUser, userVO);
            vo.setCreateUser(userVO);
            vo.setAppNum(ps.getApp());
            vo.setScriptNum(ps.getScript());
            vo.setTestcaseNum(ps.getTestcase());
            vo.setReportNum(ps.getTask());
            vos.add(vo);
        }
        return new PageResult<>(vos, projectPR.getTotal());

    }

    @ApiOperation(value="项目列表")
    @ResponseBody
    @GetMapping(value = "/list")
    public List list() {
        List<Project> projectsOfUser = projectService.findAllByUserList(currentUser.getId());
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projectsOfUser) {
            User createUser = userService.findOne(p.getCreateBy());
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(createUser, userVO);
            vo.setCreateUser(userVO);
            vos.add(vo);
        }
        return vos;
    }

    @ApiOperation(value="获得一个项目的详情（包含成员），同时表明该用户进入了该项目")
    @ResponseBody
    @GetMapping(value = "/{projectId}/detail")
    public ProjectDetailVO detail(@PathVariable Long projectId) throws Exception {
        Project project = projectValidator.validateProjectExist(projectId);
        projectValidator.validateUserIsProjectMember(projectId, currentUser.getId());

        List<User> members = projectMemberService.findAllMembers(projectId);
        viewMgr.setRecentViewProject(projectId);
        User createUser = userService.findOne(project.getCreateBy());
        //  build vo
        ProjectDetailVO vo = new ProjectDetailVO();
        ProjectVO projectVO = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(createUser, UserVO.class);
        projectVO.setCreateUser(userVO);
        vo.setProject(projectVO);
        List<UserVO> userVOS = new ArrayList<>();
        members.forEach(m -> {
            if(m != null && StringUtils.isNotBlank(m.getUsername())) {
                userVOS.add(buildVO(m, UserVO.class));
            }
        });
        vo.setProjectMembers(userVOS);
        return vo;
    }

    @ApiOperation(value="我进过的项目")
    @ResponseBody
    @GetMapping(value = "/my/views")
    public List myViews() throws Exception {

        List<Long> projectIds = viewMgr.getRecentViewProject(currentUser.getUsername());
        List<ProjectVO> vos = new ArrayList<>();
        if(projectIds != null && !projectIds.isEmpty()){
            List<Project> projects = projectService.findByProjectOrder(projectIds);
            vos = buildVOs(projects, ProjectVO.class);
        }
        return vos;

    }

    private void checkProjectOwner(Long entityId) {
        checkProjectRole(entityId, DB.ProjectRole.OWNER);
    }

    private void checkProjectAdmin(Long entityId) {
        checkProjectRole(entityId, DB.ProjectRole.ADMIN);
    }

    private void checkProjectRole(Long entityId, DB.ProjectRole projectRole) {
        ProjectMember projectMember = projectMemberService.findByProjectIdAndMemberId(entityId, currentUser.getId());
        if(projectMember != null) {
            if (!projectRole.equals(projectMember.getProjectRole())) {
                throw new AuthorizedException(ResultCode.ILLEGAL_OP, "您无法更改项目");
            }
        }
    }


}
