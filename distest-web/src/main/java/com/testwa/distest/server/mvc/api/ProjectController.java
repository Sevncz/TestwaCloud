package com.testwa.distest.server.mvc.api;

import com.testwa.distest.server.mvc.beans.PageQuery;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.ProjectVO;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.beans.Result;
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


    @ApiOperation(value="创建项目", notes="")
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public Result save(@ApiParam(required=true, name="beans", value="{'name': ''}")
                                       @RequestBody Map<String, String> projectMap){
        Project project = new Project();
        String name = projectMap.getOrDefault("name", "");
        if(StringUtils.isBlank(name)){
            return fail(ResultCode.INVALID_PARAM.getValue(), "参数错误");
        }
        User user = userService.findByUsername(getCurrentUsername());
        project.setName(name);
        project.setCreateDate(new Date());
        project.setUserId(user.getId());
        project.setUserName(user.getUsername());
        projectService.save(project);

        return ok();
    }


    @ApiOperation(value="删除项目", notes="")
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@ApiParam(required=true, name="beans", value="{'ids': [1,2,3,4]}")
                                                 @RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return fail(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确");
        }
        if (ids == null) {
            return fail(ResultCode.PARAM_ERROR.getValue(), "ids为空");
        }
        for(String id : ids){
            projectService.deleteById(id);
        }
        return ok();
    }

    @ApiOperation(value="获取项目列表，分页")
    @ResponseBody
    @RequestMapping(value = "/page", method= RequestMethod.POST)
    public Result page(@RequestBody PageQuery query){
        Map<String, Object> result = new HashMap<>();
        try{

            PageRequest pageRequest = buildPageRequest(query);
            List filters = query.filters;
            List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "_id", projectIds);
            Page<Project> projects =  projectService.find(filters, pageRequest);
            Iterator<Project> projectsIter =  projects.iterator();
//            List<Project> lists = new ArrayList<>();
            List<ProjectVO> lists = new ArrayList<>();
            while(projectsIter.hasNext()){
                lists.add(new ProjectVO(projectsIter.next()));
            }
            result.put("records", lists);
            result.put("totalRecords", projects.getTotalElements());

            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get project table error, %s", query.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), "服务器错误");
        }

    }

    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public Result list(){
        List filters = new ArrayList<>();
        List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
        List<String> projectIds = new ArrayList<>();
        projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        filters = filterProject(filters, "_id", projectIds);
        List<Project> projects = projectService.find(filters);
        List<Map<String, String>> maps = new ArrayList<>();
        for(Project a : projects){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getName());
            map.put("id", a.getId());
            maps.add(map);
        }
        return ok(maps);
    }


    @ResponseBody
    @RequestMapping(value = "/collaboration/invite", method= RequestMethod.POST)
    public Result addMember(@ApiParam(required=true, name="beans", value="{'projectId': '', 'username': ''}") @RequestBody Map<String, Object> params){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数错误");
        }
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "项目不存在");
        }
//        if(!project.getUserId().equals(user.getId())){
//            return fail(ResultCode.NO_AUTH.getValue(), "没有权限"), headers, HttpStatus.FORBIDDEN);
//        }
        User member = userService.findByUsername(username);
        if(member == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户不存在");
        }

        projectService.addMember(projectId, member);

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/collaboration/remove", method= RequestMethod.POST)
    public Result delMember(@ApiParam(required=true, name="beans", value="{'projectId': '', 'username': ''}")
                                                    @RequestBody Map<String, Object> params){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数不能为空");
        }
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "项目不存在");
        }

//        if(!project.getUserId().equals(user.getId())){
//            return new ResponseEntity<>(new CustomResponseEntity("没有权限"), headers, HttpStatus.FORBIDDEN);
//        }

        User member = userService.findByUsername(username);
        if(member == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "用户不存在");
        }

        projectService.delMember(projectId, member);

        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/collaborators/{projectId}", method= RequestMethod.GET)
    public Result members(@PathVariable String projectId){
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "项目不存在");
        }

        List<String> usernames = project.getMembers();
        return ok(usernames);
    }

}
