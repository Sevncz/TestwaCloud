package com.testwa.distest.server.web.project.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.common.form.DeleteOneForm;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.common.vo.SelectVO;
import com.testwa.distest.server.mvc.beans.PageResult;
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
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;
import java.util.*;


/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
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
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public Result save(@Valid ProjectNewForm form) throws AccountException, AccountNotFoundException, AuthorizedException, AccountAlreadyExistException, NoSuchProjectException, ParamsException {

        if(form.getMembers() != null){
            userValidator.validateUsernamesExist(form.getMembers());
        }

        Project project = projectService.save(form);
        return ok(project);
    }
    @ApiOperation(value="更新项目")
    @ResponseBody
    @RequestMapping(value = "/update", method= RequestMethod.POST)
    public Result save(@Valid ProjectUpdateForm form) throws AccountException, AccountNotFoundException, AuthorizedException, AccountAlreadyExistException, NoSuchProjectException, ParamsException {
        projectValidator.validateProjectExist(form.getProjectId());

        if(form.getMembers() != null){
            userValidator.validateUsernamesExist(form.getMembers());
        }

        Project project = projectService.update(form);
        return ok(project);
    }


    @ApiOperation(value="删除多个项目", notes="")
    @ResponseBody
    @RequestMapping(value = "/delete/all", method= RequestMethod.POST)
    public Result delete(@RequestBody @Valid DeleteAllForm form){
        projectService.deleteAll(form.getEntityIds());
        return ok();
    }


    @ApiOperation(value="删除一个项目", notes="")
    @ResponseBody
    @RequestMapping(value = "/delete/one", method= RequestMethod.POST)
    public Result delete(@RequestBody @Valid DeleteOneForm form){
        projectService.delete(form.getEntityId());
        return ok();
    }

    @ApiOperation(value="获取我的项目和我参加的项目列表，分页", notes = "http://localhost:8080/testwa/api/project/page?page=1&size=20&sortField=id&sortOrder=desc&projectName=")
    @ResponseBody
    @RequestMapping(value = "/page", method=RequestMethod.GET)
    public Result page(@Valid ProjectListForm form){
        PageResult<Project> projectPR = projectService.findByPage(form);
        PageResult<ProjectVO> pr = buildVOPageResult(projectPR, ProjectVO.class);
        return ok(pr);

    }

    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public Result list() throws AccountException {
        List<Project> projectsOfUser = projectService.findAllOfUserProject(WebUtil.getCurrentUsername());
        List<SelectVO> vos = new ArrayList<>();
        projectsOfUser.forEach(item -> {
            SelectVO vo = new SelectVO();
            vo.setId(item.getId());
            vo.setName(item.getProjectName());
            vos.add(vo);
        });
        return ok(vos);
    }

    @ResponseBody
    @RequestMapping(value = "/detail/{projectId}", method= RequestMethod.GET)
    public Result detail(@PathVariable Long projectId) throws Exception {
        Project project = projectValidator.validateProjectExist(projectId);

        List<User> members = projectMemberService.findAllMembers(projectId);

        viewMgr.setRecentViewProject(project);

        Project p = projectService.findOne(projectId);
        ProjectDetailVO vo = new ProjectDetailVO();
        ProjectVO projectVO = buildVO(p, ProjectVO.class);
        vo.setProject(projectVO);
        List<UserVO> userVOS = new ArrayList<>();
        members.forEach(m -> {
            userVOS.add(buildVO(m, UserVO.class));
        });
        vo.setProjectMembers(userVOS);
        return ok(vo);
    }

    @ResponseBody
    @GetMapping(value = "/my/views")
    public Result myViews() throws Exception {
        List<Project> projects = viewMgr.getRecentViewProject(WebUtil.getCurrentUsername());
        List<ProjectVO> vos = buildVOs(projects, ProjectVO.class);
        return ok(vos);

    }


}
