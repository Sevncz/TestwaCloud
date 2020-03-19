package com.testwa.distest.server.web.project.controller;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.form.IDForm;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.script.util.FileUtil;
import com.testwa.core.script.vo.TaskEnvVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.exception.AuthorizedException;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.fdfs.service.FdfsStorageService;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.project.service.ViewMgr;
import com.testwa.distest.server.service.task.service.TaskResultService;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;


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
    @Value("${base.report.dir}")
    private String reportDir;
    @Autowired
    private TaskResultService taskResultService;

    @ApiOperation(value="创建项目")
    @ResponseBody
    @PostMapping(value = "/save")
    public ProjectVO save(@RequestBody @Valid ProjectNewForm form) throws IOException {

        if(form.getMembers() != null && !form.getMembers().isEmpty()){
            userValidator.validateUsernamesExist(form.getMembers());
        }
        Project project = projectService.save(form, currentUser);

        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(currentUser, UserVO.class);
        vo.setCreateUser(userVO);
        initProjectReport(project.getId());
        return vo;
    }

    @ApiOperation(value="项目报告初始化")
    @ResponseBody
    @PostMapping(value = "/{projectId}/report/init")
    public String reportInit(@PathVariable Long projectId) throws IOException {
        initProjectReport(projectId);
        return "初始化成功";
    }

    @ApiOperation(value="项目报告生成")
    @ResponseBody
    @PostMapping(value = "/{projectId}/report/generate")
    public String reportInitResult(@PathVariable Long projectId) throws IOException {
        taskResultService.generateProject(projectId);
        return "操作成功";
    }

    private void initProjectReport(Long projectId) throws IOException {
        // 创建allure报告结构
        Path projectPath = Paths.get(reportDir, String.valueOf(projectId));
        if (Files.notExists(projectPath)) {
            try {
                Files.createDirectory(projectPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path resultPath = Paths.get(projectPath.toString(), "result");
        if(Files.notExists(resultPath)) {
            try {
                Files.createDirectory(resultPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path historyPath = Paths.get(projectPath.toString(), "result", "history");
        Path reportPath = Paths.get(projectPath.toString(), "report");
        // 执行allure generate ./result -o ./report/ --clean
        String[] cmd = {"allure", "generate", resultPath.toString(), "-o", reportPath.toString(), "--clean"};
        Runtime.getRuntime().exec(cmd);
        // 将histroy移动到result
        FileUtils.copyDirectory(Paths.get(reportPath.toString(), "history").toFile(), historyPath.toFile());
    }

    @ApiOperation(value="更新项目")
    @ResponseBody
    @PostMapping(value = "/update")
    public ProjectVO update(@RequestBody @Valid ProjectUpdateForm form) {
        projectValidator.validateProjectExist(form.getProjectId());

        projectValidator.checkProjectAdmin(form.getProjectId(), currentUser.getId());

        if(form.getMembers() != null){
            userValidator.validateUsernamesExist(form.getMembers());
        }
        Project project = projectService.update(form);
        User createUser = userService.get(project.getCreateBy());

        ProjectVO vo = buildVO(project, ProjectVO.class);
        UserVO userVO = buildVO(createUser, UserVO.class);
        vo.setCreateUser(userVO);

        return vo;
    }


    @ApiOperation(value="删除多个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/deleteAll")
    public void deleteAll(@RequestBody @Valid IDListForm form) {
        form.getEntityIds().forEach(entityId -> {
            projectValidator.checkProjectAdmin(entityId, currentUser.getId());
        });
        projectService.delete(form.getEntityIds());
    }


    @ApiOperation(value="删除一个项目", notes="")
    @ResponseBody
    @PostMapping(value = "/deleteOne")
    public void deleteOne(@RequestBody @Valid IDForm form) {
        projectValidator.checkProjectAdmin(form.getEntityId(), currentUser.getId());
        projectService.delete(form.getEntityId());
    }

    @ApiOperation(value="获取我的项目和我参加的项目列表，分页")
    @ResponseBody
    @GetMapping(value = "/page")
    public PageResult page(@Valid ProjectListForm form){
        PageResult<Project> projectPR = projectService.pageByUser(form, currentUser);
        List<Project> projects = projectPR.getPages();
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projects) {
            User createUser = userService.get(p.getCreateBy());
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
        List<Project> projectsOfUser = projectService.listByUser(currentUser.getId());
        List<ProjectVO> vos = new ArrayList<>();
        for (Project p : projectsOfUser) {
            User createUser = userService.get(p.getCreateBy());
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

        List<User> members = projectMemberService.listMembers(projectId);
        viewMgr.setRecentViewProject(projectId);
        User createUser = userService.get(project.getCreateBy());
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

        List<Long> projectIds = viewMgr.getRecentViewProject();
        List<ProjectVO> vos = new ArrayList<>();
        if(projectIds != null && !projectIds.isEmpty()){
            List<Project> projects = projectService.findByProjectOrder(projectIds);
            vos = buildVOs(projects, ProjectVO.class);
        }
        return vos;

    }
}
