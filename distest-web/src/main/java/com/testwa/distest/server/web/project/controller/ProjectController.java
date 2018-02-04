package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.form.DeleteOneForm;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.project.service.ViewMgr;
import com.testwa.distest.server.web.auth.validator.UserValidator;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectDetailVO;
import com.testwa.distest.server.web.project.vo.ProjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("项目操作相关api")
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
    private ViewMgr viewMgr;

    @ApiOperation(value="创建项目")
    @ResponseBody
    @PostMapping(value = "/save")
    public Result save(@RequestBody @Valid ProjectNewForm form) throws AuthorizedException, ParamsException, ObjectNotExistsException {

        if(form.getMembers() != null && form.getMembers().size() > 0){
            userValidator.validateUsernamesExist(form.getMembers());
        }

        Project project = projectService.save(form);

        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(project.getCreateUser(), UserVO.class);
        vo.setCreateUser(userVO);
        return ok(vo);
    }
    @ApiOperation(value="更新项目")
    @ResponseBody
    @PostMapping(value = "/update")
    public Result update(@RequestBody @Valid ProjectUpdateForm form) throws ObjectNotExistsException, AuthorizedException, ParamsException {
        projectValidator.validateProjectExist(form.getProjectId());

        if(form.getMembers() != null){
            userValidator.validateUsernamesExist(form.getMembers());
        }

        Project project = projectService.update(form);
        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(project.getCreateUser(), UserVO.class);
        vo.setCreateUser(userVO);
        return ok(vo);
    }


    @ApiOperation(value="删除多个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/delete/all")
    public Result deleteAll(@RequestBody @Valid DeleteAllForm form) throws ParamsIsNullException {
        if(form.getEntityIds() == null && form.getEntityIds().size() == 0){
            throw new ParamsIsNullException("参数不能为空");
        }
        projectService.deleteAll(form.getEntityIds());
        return ok();
    }


    @ApiOperation(value="删除一个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/delete/one")
    public Result deleteOne(@RequestBody @Valid DeleteOneForm form) throws ParamsIsNullException {
        if(form.getEntityId() == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        projectService.delete(form.getEntityId());
        return ok();
    }

    @ApiOperation(value="获取我的项目和我参加的项目列表，分页")
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid ProjectListForm form){
        PageResult<Project> projectPR = projectService.findAllByUserPage(form, WebUtil.getCurrentUsername());
        List<Project> projects = projectPR.getPages();
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projects) {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(p.getCreateUser(), userVO);
            vo.setCreateUser(userVO);
            vos.add(vo);
        }
        PageResult<ProjectVO> pr = new PageResult<>(vos, projectPR.getTotal());
        return ok(pr);

    }

    @ApiOperation(value="项目列表")
    @ResponseBody
    @GetMapping(value = "/select/list")
    public Result selectList() throws AccountException {
        List<Project> projectsOfUser = projectService.findAllByUserList(WebUtil.getCurrentUsername());
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projectsOfUser) {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(p.getCreateUser(), userVO);
            vo.setCreateUser(userVO);
            vos.add(vo);
        }
        return ok(vos);
    }

    @ApiOperation(value="获得一个项目的详情（包含成员），同时表明该用户进入了该项目")
    @ResponseBody
    @GetMapping(value = "/detail/{projectId}")
    public Result detail(@PathVariable Long projectId) throws Exception {
        Project project = projectValidator.validateProjectExist(projectId);

        List<User> members = projectMemberService.findAllMembers(projectId);

        viewMgr.setRecentViewProject(projectId);

        Project p = projectService.fetchOne(projectId);
        //  build vo
        ProjectDetailVO vo = new ProjectDetailVO();
        ProjectVO projectVO = buildVO(p, ProjectVO.class);
        UserVO userVO = buildVO(p.getCreateUser(), UserVO.class);
        projectVO.setCreateUser(userVO);
        vo.setProject(projectVO);
        List<UserVO> userVOS = new ArrayList<>();
        members.forEach(m -> {
            userVOS.add(buildVO(m, UserVO.class));
        });
        vo.setProjectMembers(userVOS);
        return ok(vo);
    }

    @ApiOperation(value="我进过的项目")
    @ResponseBody
    @GetMapping(value = "/my/views")
    public Result myViews() throws Exception {
        List<Long> projectIds = viewMgr.getRecentViewProject(WebUtil.getCurrentUsername());
        List<ProjectVO> vos = new ArrayList<>();
        if(projectIds != null && projectIds.size() > 0){
            List<Project> projects = projectService.findByProjectOrder(projectIds);
            vos = buildVOs(projects, ProjectVO.class);
        }
        return ok(vos);

    }


}