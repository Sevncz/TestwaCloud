package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.mvc.model.App;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.params.QueryTableFilterParams;
import com.testwa.distest.server.mvc.service.AppService;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.api.VO.AppVO;
import com.testwa.distest.server.mvc.model.message.ResultCode;
import com.testwa.distest.server.mvc.model.message.Result;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private AppService appService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;


    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST, produces={"application/json"})
    public Result save(@RequestBody Map<String, String> appMap){
        String appId = appMap.getOrDefault("id", "");
        String projectId = appMap.getOrDefault("projectId", "");
        String version = appMap.getOrDefault("version", "");
        if(StringUtils.isBlank(appId)
                || StringUtils.isBlank(version)
                || StringUtils.isBlank(projectId)){
            log.error("appId: {}, version: {}, projectId: {}", appId, version, projectId);
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数错误");
        }
        App app = appService.getAppById(appId);
        if(app == null){
            log.error("AppId get app was null", appId);
            return fail(ResultCode.SERVER_ERROR.getValue(), "App找不到");
        }
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            log.error("ProjectId get project was null", projectId);
            return fail(ResultCode.SERVER_ERROR.getValue(), "项目id错误,找不到对应的项目");
        }

        User currentUser = userService.findByUsername(getCurrentUsername());

        app.setProjectId(projectId);
        app.setProjectName(project.getName());
        app.setVersion(version);
        app.setUserId(currentUser.getId());
        app.setUsername(currentUser.getUsername());
        app.setDisable(true);
        appService.update(app);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value="/upload-app", method= RequestMethod.POST)
    public Result upload(@RequestParam("file") MultipartFile uploadfile){
        Map<String, String> result = new HashMap<>();
        if(uploadfile.isEmpty()){
            return fail(ResultCode.SERVER_ERROR.getValue(), "文件是空");
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
            App app = appService.saveApp(filename, aliasName, filepath.toString(), size, type);
            result.put("id", app.getId());
            result.put("name", app.getName());
            result.put("packageName", app.getPackageName());
            result.put("activity", app.getActivity());
            result.put("type", app.getType());
            result.put("sdkVersion", app.getSdkVersion());
            result.put("targetSdkVersion", app.getTargetSdkVersion());
            return ok(result);
        }
        catch (Exception e) {
            log.error(String.format("upload app error %s", uploadfile.getSize()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), "服务器异常");
        }
    }



    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST, produces={"application/json"})
    public Result delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return fail(ResultCode.PARAM_ERROR.getValue(), "ids参数格式不正确");
        }
        if (ids == null) {
            return ok();
        }
        for(String id : ids){
            appService.deleteById(id);
        }
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST, produces={"application/json"})
    public Result tableList(@RequestBody QueryTableFilterParams filter){
        Map<String, Object> result = new HashMap<>();
        try{
            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
//            filterDisable(filters);
            User user = userService.findByUsername(getCurrentUsername());
            List<Project> projectsOfUser = projectService.findByUser(user);
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<App> testwaScripts =  appService.find(filters, pageRequest);
            Iterator<App> testwaScriptsIter =  testwaScripts.iterator();
            List<AppVO> lists = new ArrayList<>();
            while(testwaScriptsIter.hasNext()){
                lists.add(new AppVO(testwaScriptsIter.next()));
            }
            result.put("records", lists);
            result.put("totalRecords", testwaScripts.getTotalElements());
            return ok(result);
        }catch (Exception e){
            log.error(String.format("Get scripts table error, %s", filter.toString()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }

    }


    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET, produces={"application/json"})
    public Result list(){
        Map<String, Object> result = new HashMap<>();

        List filters = new ArrayList<>();
        User currentUser = userService.findByUsername(getCurrentUsername());
        List<Project> projectsOfUser = projectService.findByUser(currentUser);
        List<String> projectIds = new ArrayList<>();
        projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        filters = filterProject(filters, "projectId", projectIds);

        List<App> apps = appService.find(filters);
        List<Map<String, String>> maps = new ArrayList<>();
        for(App a : apps){
            Map<String, String> map = new HashMap<>();
            map.put("name", a.getName());
            map.put("id", a.getId());
            maps.add(map);
        }
        result.put("records", maps);
        return ok(result);
    }

}
