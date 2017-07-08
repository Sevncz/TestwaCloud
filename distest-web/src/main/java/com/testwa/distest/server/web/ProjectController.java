package com.testwa.distest.server.web;

import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.TestwaProjectService;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.web.VO.ProjectVO;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by wen on 16/9/1.
 */
@Api("项目相关api")
@RestController
@RequestMapping(path = "project", produces={"application/json"})
public class ProjectController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    @Autowired
    private TestwaProjectService projectService;
    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(method= RequestMethod.GET)
    Result index(HttpServletRequest req) {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setData("Welcome, this is project index.");
        r.setMessage("index");
        r.setUrl(req.getRequestURL().toString());
        return ok(r);
    }

    @ApiOperation(value="生成一个项目", notes="")
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public Result save(@ApiParam(required=true, name="params", value="{'name': ''}")
                                           @RequestBody Map<String, String> projectMap,
                                       @ApiIgnore @CurrentUser User user,
                                       @ApiIgnore HttpServletRequest req){
        Map<String, Object> result = new HashMap<>();
        TestwaProject testwaProject = new TestwaProject();
        String name = projectMap.getOrDefault("name", "");
        if(StringUtils.isBlank(name)){
            return fail(ResultCode.INVALID_PARAM.getValue(), "参数错误");
        }
        testwaProject.setName(name);
        testwaProject.setCreateDate(new Date());
        testwaProject.setUserId(user.getId());
        testwaProject.setUserName(user.getUsername());
        projectService.save(testwaProject);

        return ok(result);
    }


    @ApiOperation(value="删除一个项目", notes="")
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@ApiParam(required=true, name="params", value="{'ids': [1,2,3,4]}")
                                                 @RequestBody Map<String, Object> params){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
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
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST)
    public Result tableList(@RequestBody QueryTableFilterParams filter,
                                            @ApiIgnore @CurrentUser User user){
        Map<String, Object> result = new HashMap<>();
        try{

            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith, in
//            List filters = (List) params.get("filters");
            List filters = filter.filters;
//            filterDisable(filters);
            List<TestwaProject> projectsOfUser = projectService.findByUser(user);
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "_id", projectIds);
            Page<TestwaProject> projects =  projectService.find(filters, pageRequest);
            Iterator<TestwaProject> projectsIter =  projects.iterator();
//            List<TestwaProject> lists = new ArrayList<>();
            List<ProjectVO> lists = new ArrayList<>();
            while(projectsIter.hasNext()){
                lists.add(new ProjectVO(projectsIter.next()));
            }
            result.put("records", lists);
            result.put("totalRecords", projects.getTotalElements());

            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get project table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), "服务器错误");
        }

    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public Result list(@ApiIgnore @CurrentUser User user){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
        Map<String, Object> result = new HashMap<>();

        List filters = new ArrayList<>();
        List<TestwaProject> projectsOfUser = projectService.findByUser(user);
        List<String> projectIds = new ArrayList<>();
        projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        filters = filterProject(filters, "_id", projectIds);
        List<TestwaProject> projects = projectService.find(filters);
        List<Map<String, String>> maps = new ArrayList<>();
        for(TestwaProject a : projects){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getName());
            map.put("id", a.getId());
            maps.add(map);
        }
        result.put("records", maps);
        return ok(result);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/member/add", method= RequestMethod.POST)
    public Result addMember(@ApiParam(required=true, name="params", value="{'projectId': '', 'username': ''}")
                                                    @RequestBody Map<String, Object> params){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数错误");
        }
        TestwaProject project = projectService.getProjectById(projectId);
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


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/member/del", method= RequestMethod.POST)
    public Result delMember(@ApiParam(required=true, name="params", value="{'projectId': '', 'username': ''}")
                                                    @RequestBody Map<String, Object> params, @ApiIgnore @CurrentUser User user){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数不能为空");
        }
        TestwaProject project = projectService.getProjectById(projectId);
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


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/members/{projectId}", method= RequestMethod.GET)
    public Result members(@PathVariable String projectId){
        Map<String, Object> result = new HashMap<>();
        TestwaProject project = projectService.getProjectById(projectId);
        if(project == null){
            return fail(ResultCode.PARAM_ERROR.getValue(), "项目不存在");
        }

        List<String> usernames = project.getMembers();
        result.put("records", usernames);
        return ok(result);
    }

}
