package com.testwa.distest.server.web;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.TestwaAppService;
import com.testwa.distest.server.service.TestwaProjectService;
import com.testwa.distest.server.web.VO.AppVO;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.ResultInfo;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by wen on 16/9/2.
 */
@Api("应用相关api")
@RestController
@RequestMapping(path = "app")
public class AppController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private TestwaAppService testwaAppService;
    @Autowired
    private TestwaProjectService testwaProjectService;

    @Autowired
    private Environment env;

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> save(@RequestBody Map<String, String> appMap, @ApiIgnore @CurrentUser User user){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
        String appId = appMap.getOrDefault("id", "");
        String projectId = appMap.getOrDefault("projectId", "");
        String version = appMap.getOrDefault("version", "");
        if(StringUtils.isBlank(appId)
                || StringUtils.isBlank(version)
                || StringUtils.isBlank(projectId)){
            log.error("appId: {}, version: {}, projectId: {}", appId, version, projectId);
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数错误"), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaApp app = testwaAppService.getAppById(appId);
        if(app == null){
            log.error("AppId get app was null", appId);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "App找不到"), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaProject project = testwaProjectService.getProjectById(projectId);
        if(project == null){
            log.error("ProjectId get project was null", projectId);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "项目id错误,找不到对应的项目"), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        app.setProjectId(projectId);
        app.setProjectName(project.getName());
        app.setVersion(version);
        app.setUserId(user.getId());
        app.setUsername(user.getUsername());
        app.setDisable(true);
        testwaAppService.update(app);
        return new ResponseEntity<>(successInfo(),headers, HttpStatus.CREATED);
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value="/upload-app", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> upload(@RequestParam("file") MultipartFile uploadfile){
        Map<String, String> result = new HashMap<>();
        if(uploadfile.isEmpty()){
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "文件是空"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            String filename = uploadfile.getOriginalFilename();
            String aliasName = PinYinTool.getPingYin(filename);
            Path dir = Paths.get(env.getProperty("app.save.path"), Identities.uuid2());
            Files.createDirectories(dir);
            Path filepath = Paths.get(dir.toString(), aliasName);
            Files.write(filepath, uploadfile.getBytes(), StandardOpenOption.CREATE);

            String type = filename.substring(filename.lastIndexOf(".") + 1);

            String size = uploadfile.getSize() + "";
            TestwaApp app = testwaAppService.saveApp(filename, aliasName, filepath.toString(), size, type);
            result.put("id", app.getId());
            result.put("name", app.getName());
            result.put("packageName", app.getPackageName());
            result.put("activity", app.getActivity());
            result.put("type", app.getType());
            result.put("sdkVersion", app.getSdkVersion());
            result.put("targetSdkVersion", app.getTargetSdkVersion());
            return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(String.format("upload app error %s", uploadfile.getSize()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), "服务器异常"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ids == null) {
            return new ResponseEntity<>(successInfo(), HttpStatus.OK);
        }
        for(String id : ids){
            testwaAppService.deleteById(id);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> tableList(@RequestBody QueryTableFilterParams filter, @ApiIgnore @CurrentUser User user){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=utf-8");
        Map<String, Object> result = new HashMap<>();
        try{
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
//            filterDisable(filters);
            List<TestwaProject> projectsOfUser = testwaProjectService.findByUser(user);
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<TestwaApp> testwaScripts =  testwaAppService.find(filters, pageRequest);
            Iterator<TestwaApp> testwaScriptsIter =  testwaScripts.iterator();
            List<AppVO> lists = new ArrayList<>();
            while(testwaScriptsIter.hasNext()){
                lists.add(new AppVO(testwaScriptsIter.next()));
            }
            result.put("records", lists);
            result.put("totalRecords", testwaScripts.getTotalElements());
            return new ResponseEntity<>(dataInfo(result), headers, HttpStatus.OK);
        }catch (Exception e){
            log.error(String.format("Get scripts table error, %s", filter.toString()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<ResultInfo> list(@ApiIgnore @CurrentUser User user){
        Map<String, Object> result = new HashMap<>();

        List filters = new ArrayList<>();
        List<TestwaProject> projectsOfUser = testwaProjectService.findByUser(user);
        List<String> projectIds = new ArrayList<>();
        projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        filters = filterProject(filters, "projectId", projectIds);

        List<TestwaApp> apps = testwaAppService.find(filters);
        List<Map<String, String>> maps = new ArrayList<>();
        for(TestwaApp a : apps){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getName());
            map.put("id", a.getId());
            maps.add(map);
        }
        result.put("records", maps);
        return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
    }

}
