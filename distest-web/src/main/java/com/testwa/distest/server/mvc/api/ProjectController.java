package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.beans.*;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.ProjectDetailVO;
import com.testwa.distest.server.mvc.vo.ProjectVO;
import com.testwa.distest.server.mvc.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by wen on 16/9/1.
 */
@Api("项目相关api")
@RestController
@RequestMapping(path = "/api/project", produces={"application/json"})
public class ProjectController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;


    @SuppressWarnings("unused")
    private static class ProjectInfo {
        public String projectName;
        public String description;

        @Override
        public String toString() {
            return "ProjectInfo{" +
                    "projectName='" + projectName + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @ApiOperation(value="创建项目", notes="参数：{\"projectName\": \"name\", \"description\": \"desc\"}")
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public Result save(@RequestBody ProjectInfo projectInfo){
        Project project = new Project();
        String projectName = projectInfo.projectName;
        if(StringUtils.isBlank(projectName)){
            log.error("params projectName is none");
            return fail(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        String description = projectInfo.description;
        User user = userService.findByUsername(getCurrentUsername());
        if(user == null){
            log.error("login user not found");
            return fail(ResultCode.NO_LOGIN, "请重新登录");
        }
        project.setProjectName(projectName);
        project.setDescription(description);
        project.setCreateTime(TimeUtil.getTimestampLong());
        project.setUserId(user.getId());
        project.setUsername(user.getUsername());
        projectService.save(project);
        return ok(project);
    }


    @ApiOperation(value="删除项目", notes="")
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@RequestBody DelParams del){
        List<String> ids = del.ids;
        if (ids == null || ids.size() == 0) {
            log.error("ids is null");
            return fail(ResultCode.INVALID_PARAM, "参数为空");
        }
        for(String id : ids){
            projectService.deleteById(id);
        }
        return ok();
    }

    @ApiOperation(value="获取我的项目和我参加的项目列表，分页", notes = "http://localhost:8080/testwa/api/project/page?page=1&size=20&sortField=id&sortOrder=desc&projectName=")
    @ResponseBody
    @RequestMapping(value = "/page", method=RequestMethod.GET)
    public Result page(@RequestParam(value = "page")Integer page,
                       @RequestParam(value = "size")Integer size ,
                       @RequestParam(value = "sortField")String sortField,
                       @RequestParam(value = "sortOrder")String sortOrder,
                       @RequestParam(required=false) String projectName){
        try{

            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
            List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            Page<Project> projects =  projectService.findPage(pageRequest, projectIds, projectName);
            Iterator<Project> projectsIter =  projects.iterator();
            List<ProjectVO> lists = new ArrayList<>();
            while(projectsIter.hasNext()){
                lists.add(new ProjectVO(projectsIter.next()));
            }
            PageResult<ProjectVO> pr = new PageResult<>(lists, projects.getTotalElements());
            return ok(pr);
        }catch (Exception e){
            log.error("Get project table error", e);
            return fail(ResultCode.SERVER_ERROR, "服务器错误");
        }

    }

    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public Result list(){
        List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
        List<String> projectIds = new ArrayList<>();
        projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        List<Project> projects = projectService.findAll(projectIds);
        List<Map<String, String>> maps = new ArrayList<>();
        for(Project a : projects){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getProjectName());
            map.put("id", a.getId());
            maps.add(map);
        }
        return ok(maps);
    }


    @ResponseBody
    @RequestMapping(value = "/detail/{projectId}", method= RequestMethod.GET)
    public Result detail(@PathVariable String projectId){
        Project p = projectService.findById(projectId);
        if(p == null){
            log.error("project not found, {}", projectId);
            return fail(ResultCode.INVALID_PARAM, "项目不存在");
        }
        List<User> users = projectService.getUserMembersByProject(projectId);
        ProjectDetailVO vo = new ProjectDetailVO(p, users);
        return ok(vo);
    }

    @SuppressWarnings("unused")
    private static class MembersParams {
        public String projectId;
        public List<String> usernames;

        @Override
        public String toString() {
            return "MembersParams{" +
                    "projectId='" + projectId + '\'' +
                    ", usernames=" + usernames +
                    '}';
        }
    }

    @ResponseBody
    @RequestMapping(value = "/member/add", method= RequestMethod.POST)
    public Result addMember(@RequestBody MembersParams params){
        if(StringUtils.isBlank(params.projectId) || params.usernames == null || params.usernames.size() == 0){
            log.error("params is none, {}", params);
            return fail(ResultCode.INVALID_PARAM, "参数不能为空");
        }
        Project project = projectService.getProjectById(params.projectId);
        if(project == null){
            log.error("project not found, {}", params.projectId);
            return fail(ResultCode.INVALID_PARAM, "项目不存在");
        }
        if(params.usernames.contains(getCurrentUsername())){
            log.error("can not add self, {}", params.toString());
            return fail(ResultCode.INVALID_PARAM, "无法添加自己，你已经在项目中");
        }


        User owner = userService.findByUsername(getCurrentUsername());

        if(!project.getUserId().equals(owner.getId())){
            log.error("login user not owner of the project, projectId: {}, currentUsername: {}", params.projectId, getCurrentUsername());
            return fail(ResultCode.NO_AUTH, "您不是项目所有者，无法删除项目成员");
        }

        for(String username : params.usernames){

            User member = userService.findByUsername(username);
            if(member == null){
                log.error("member not found, {}", username);
                return fail(ResultCode.PARAM_ERROR, "用户不存在");
            }

            List<ProjectMember> pm = projectService.getMembersByProjectAndUsername(params.projectId, member.getId());
            if(pm != null && pm.size() > 0){
                log.error("ProjectMember is not null, {}", username);
                return fail(ResultCode.PARAM_ERROR, String.format("用户%s已在项目中", username));
            }
            projectService.addMember(params.projectId, member.getId());
        }

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/member/remove", method= RequestMethod.POST)
    public Result delMember(@RequestBody MembersParams params){
        if(StringUtils.isBlank(params.projectId) || params.usernames == null || params.usernames.size() == 0){
            log.error("params is none, {}", params);
            return fail(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        Project project = projectService.getProjectById(params.projectId);
        if(project == null){
            log.error("project not found, {}", params.projectId);
            return fail(ResultCode.PARAM_ERROR, "项目不存在");
        }
        if(params.usernames.contains(getCurrentUsername())){
            log.error("can not add self, {}", params.toString());
            return fail(ResultCode.INVALID_PARAM, "无法删除自己");
        }

        User owner = userService.findByUsername(getCurrentUsername());

        if(!project.getUserId().equals(owner.getId())){
            log.error("login user not owner of the project, projectId: {}, currentUsername: {}", params.projectId, getCurrentUsername());
            return fail(ResultCode.NO_AUTH, "您不是项目所有者，无法删除项目成员");
        }

        for(String username : params.usernames){

            User member = userService.findByUsername(username);
            if(member == null){
                log.error("member not found, {}", username);
                return fail(ResultCode.PARAM_ERROR, "用户不存在");
            }

            projectService.delMember(params.projectId, member.getId());
        }

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/members/{projectId}", method= RequestMethod.GET)
    public Result members(@PathVariable String projectId){
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            log.error("project not found, {}", projectId);
            return fail(ResultCode.PARAM_ERROR, "项目不存在");
        }

        List<User> users = projectService.getUserMembersByProject(projectId);
        List<UserVO> vo = new ArrayList<>();
        for(User u : users){
            vo.add(new UserVO(u));
        }
        return ok(vo);
    }



    @ResponseBody
    @RequestMapping(value = "/member/query", method= RequestMethod.GET)
    public Result queryMember(@RequestParam(value = "projectId")String projectId,
                              @RequestParam(value = "memberName")String memberName){
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            log.error("project not found, {}", projectId);
            return fail(ResultCode.PARAM_ERROR, "项目不存在");
        }
        Map<String, List<User>> users = projectService.getMembers(projectId, memberName);
        Map<String, List<UserVO>> vo = new HashMap<>();

        for(String key : users.keySet()){
            List<UserVO> v = new ArrayList<>();
            for(User u : users.get(key)){
                v.add(new UserVO(u));
            }
            vo.put(key, v);
        }
        return ok(vo);
    }

}
