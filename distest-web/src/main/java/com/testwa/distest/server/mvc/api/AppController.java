package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.mvc.beans.*;
import com.testwa.distest.server.mvc.model.App;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.AppService;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.AppVO;
import com.testwa.distest.server.mvc.vo.ProjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping(path = "/api/app")
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


    @SuppressWarnings("unused")
    private static class AppInfo {
        public String appId;
        public String projectId;
        public String version;

    }

    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST, produces={"application/json"})
    public Result save(@RequestBody AppInfo appInfo){
        String appId = appInfo.appId;
        String projectId = appInfo.projectId;
        String version = appInfo.version;
        if(StringUtils.isBlank(appId)
                || StringUtils.isBlank(version)
                || StringUtils.isBlank(projectId)){
            log.error("appId: {}, version: {}, projectId: {}", appId, version, projectId);
            return fail(ResultCode.PARAM_ERROR, "参数错误");
        }
        App app = appService.getAppById(appId);
        if(app == null){
            log.error("AppId get app was null", appId);
            return fail(ResultCode.PARAM_ERROR, "App找不到");
        }
        Project project = projectService.getProjectById(projectId);
        if(project == null){
            log.error("ProjectId get project was null", projectId);
            return fail(ResultCode.SERVER_ERROR, "项目id错误,找不到对应的项目");
        }

        User currentUser = userService.findByUsername(getCurrentUsername());

        app.setProjectId(projectId);
        app.setProjectName(project.getProjectName());
        app.setVersion(version);
        app.setUserId(currentUser.getId());
        app.setUsername(currentUser.getUsername());
        app.setDisable(false);
        appService.update(app);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value="/upload", method= RequestMethod.POST)
    public Result upload(@RequestParam("file") MultipartFile uploadfile){
        Map<String, String> result = new HashMap<>();
        if(uploadfile.isEmpty()){
            return fail(ResultCode.PARAM_ERROR, "文件是空");
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
            return fail(ResultCode.SERVER_ERROR, "服务器异常");
        }
    }



    @ApiOperation(value="删除应用", notes="")
    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public Result delete(@RequestBody DelParams del){
        List<String> ids = del.ids;
        if (ids == null || ids.size() == 0) {
            log.error("ids is null");
            return fail(ResultCode.INVALID_PARAM, "参数为空");
        }
        for(String id : ids){
            appService.deleteById(id);
        }
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/page", method= RequestMethod.GET)
    public Result page(@RequestParam(value = "page")Integer page,
                        @RequestParam(value = "size")Integer size ,
                        @RequestParam(value = "sortField")String sortField,
                        @RequestParam(value = "sortOrder")String sortOrder,
                        @RequestParam(required=false) String projectId,
                        @RequestParam(required=false) String appName){
        try{
            PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
            User user = userService.findByUsername(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            if(StringUtils.isBlank(projectId)){
                List<Project> projectsOfUser = projectService.findByUser(user);
                projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            }else{
                List<ProjectMember> pms = projectService.getMembersByProjectAndUserId(projectId, user.getId());
                if(pms == null || pms.size() == 0){
                    log.error("ProjectMember is null, user {} not in project {}", user.getId(), projectId);
                    return fail(ResultCode.INVALID_PARAM, "用户不属于该项目");
                }
                projectIds.add(projectId);
            }
            Page<App> apps = appService.findPage(pageRequest, projectIds, appName);
            Iterator<App> appIter = apps.iterator();
            List<AppVO> lists = new ArrayList<>();
            while(appIter.hasNext()){
                lists.add(new AppVO(appIter.next()));
            }
            PageResult<AppVO> pr = new PageResult<>(lists, apps.getTotalElements());
            return ok(pr);
        }catch (Exception e){
            log.error("Get app page error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }

    }


    @ResponseBody
    @RequestMapping(value = "/list", method= RequestMethod.GET, produces={"application/json"})
    public Result list(@RequestParam(required=false) String projectId,
                        @RequestParam(required=false) String appName){
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = new ArrayList<>();
        if(StringUtils.isBlank(projectId)){
            List<Project> projectsOfUser = projectService.findByUser(user);
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        }else{
            List<ProjectMember> pms = projectService.getMembersByProjectAndUserId(projectId, user.getId());
            if(pms == null || pms.size() == 0){
                log.error("ProjectMember is null, user {} not in project {}", user.getId(), projectId);
                return fail(ResultCode.INVALID_PARAM, "用户不属于该项目");
            }
            projectIds.add(projectId);
        }
        List<App> apps = appService.find(projectIds, appName);
        List<AppVO> lists = new ArrayList<>();
        for(App app : apps){
            lists.add(new AppVO(app));
        }
        return ok(lists);
    }

}
