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
import com.testwa.distest.server.model.message.ResultInfo;
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
    ResponseEntity<ResultInfo> index(HttpServletRequest req) {
        ResultInfo<String> r = new ResultInfo<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setData("Welcome, this is project index.");
        r.setMessage("index");
        r.setUrl(req.getRequestURL().toString());
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    @ApiOperation(value="生成一个项目", notes="")
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> save(@ApiParam(required=true, name="params", value="{'name': ''}")
                                           @RequestBody Map<String, String> projectMap,
                                           @ApiIgnore @CurrentUser User user,
                                           @ApiIgnore HttpServletRequest req){
        Map<String, Object> result = new HashMap<>();
        TestwaProject testwaProject = new TestwaProject();
        String name = projectMap.getOrDefault("name", "");
        if(StringUtils.isBlank(name)){
            return new ResponseEntity<>(errorInfo(ResultCode.INVALID_PARAM.getValue(), "参数错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        testwaProject.setName(name);
        testwaProject.setCreateDate(new Date());
        testwaProject.setUserId(user.getId());
        testwaProject.setUserName(user.getUsername());
        projectService.save(testwaProject);

        return new ResponseEntity<>(dataInfo(result), HttpStatus.CREATED);
    }


    @ApiOperation(value="删除一个项目", notes="")
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> delete(@ApiParam(required=true, name="params", value="{'ids': [1,2,3,4]}")
                                                 @RequestBody Map<String, Object> params){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确"), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ids == null) {
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "ids为空"), headers, HttpStatus.OK);
        }
        for(String id : ids){
            projectService.deleteById(id);
        }
        return new ResponseEntity<>(successInfo(), headers, HttpStatus.OK);
    }

    @ApiOperation(value="获取项目列表，分页")
    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> tableList(@RequestBody QueryTableFilterParams filter,
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

            return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
        }catch (Exception e){
            log.error(String.format("Get project table error, %s", filter.toString()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "服务器错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> list(@ApiIgnore @CurrentUser User user){
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
        return new ResponseEntity<>(dataInfo(result), headers, HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/member/add", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> addMember(@ApiParam(required=true, name="params", value="{'projectId': '', 'username': ''}")
                                                    @RequestBody Map<String, Object> params){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaProject project = projectService.getProjectById(projectId);
        if(project == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "项目不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
//        if(!project.getUserId().equals(user.getId())){
//            return new ResponseEntity<>(errorInfo(ResultCode.NO_AUTH.getValue(), "没有权限"), headers, HttpStatus.FORBIDDEN);
//        }
        User member = userService.findByUsername(username);
        if(member == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        projectService.addMember(projectId, member);

        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/member/del", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> delMember(@ApiParam(required=true, name="params", value="{'projectId': '', 'username': ''}")
                                                    @RequestBody Map<String, Object> params, @ApiIgnore @CurrentUser User user){
        String projectId = (String) params.getOrDefault("projectId", "");
        String username = (String) params.getOrDefault("username", "");
        if(StringUtils.isBlank(projectId) || StringUtils.isBlank(username)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数不能为空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaProject project = projectService.getProjectById(projectId);
        if(project == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "项目不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

//        if(!project.getUserId().equals(user.getId())){
//            return new ResponseEntity<>(new CustomResponseEntity("没有权限"), headers, HttpStatus.FORBIDDEN);
//        }

        User member = userService.findByUsername(username);
        if(member == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "用户不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        projectService.delMember(projectId, member);

        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/members/{projectId}", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> members(@PathVariable String projectId){
        Map<String, Object> result = new HashMap<>();
        TestwaProject project = projectService.getProjectById(projectId);
        if(project == null){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "项目不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<String> usernames = project.getMembers();
        result.put("records", usernames);
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }

}
