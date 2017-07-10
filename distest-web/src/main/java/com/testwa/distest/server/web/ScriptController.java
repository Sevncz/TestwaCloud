package com.testwa.distest.server.web;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.model.App;
import com.testwa.distest.server.model.Project;
import com.testwa.distest.server.model.Script;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.AppService;
import com.testwa.distest.server.service.ProjectService;
import com.testwa.distest.server.service.ScriptService;
import com.testwa.distest.server.service.UserService;
import com.testwa.distest.server.web.VO.ScriptVO;
import com.testwa.distest.server.model.message.ResultCode;
import com.testwa.distest.server.model.message.Result;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/9/2.
 */
@Api("脚本相关api")
@RestController
@RequestMapping(path = "script")
public class ScriptController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(ScriptController.class);

    @Autowired
    private ScriptService scriptService;
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
    public Result save(@RequestBody Map<String, String> params){
        String appId = params.getOrDefault("appId", "");
        String id = params.getOrDefault("id", "");
        if(StringUtils.isBlank(appId)
                || StringUtils.isBlank(id)){
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数错误");
        }
        Script script = scriptService.getScriptById(id);
        if(script == null){
            log.error("ScriptId get script was null", id);
            return fail(ResultCode.PARAM_ERROR.getValue(),"ScriptId找不到");
        }
        App app = appService.getAppById(appId);
        if(app == null){
            log.error("AppId get app was null", appId);
            return fail(ResultCode.PARAM_ERROR.getValue(),"app不存在");
        }
        User user = userService.findByUsername(getCurrentUsername());
        script.setAppId(appId);
        script.setProjectId(app.getProjectId());
        script.setDisable(true);
        script.setUserId(user.getId());
        script.setUsername(user.getUsername());
        scriptService.save(script);
        return ok();
    }



    @ResponseBody
    @RequestMapping(value="/upload-script", method= RequestMethod.POST)
    public Result upload(@RequestParam("file") MultipartFile uploadfile){
        Map<String, String> result = new HashMap<>();
        if(uploadfile.isEmpty()){
            return fail(ResultCode.PARAM_ERROR.getValue(), "文件为空");
        }

        try {
            String filename = uploadfile.getOriginalFilename();
            String aliasName = PinYinTool.getPingYin(filename);
            Path dir = Paths.get(env.getProperty("script.save.path"), Identities.uuid2());
            if(!Files.exists(dir)){
                Files.createDirectories(dir);
            }
            Path filepath = Paths.get(dir.toString(), aliasName);
            Files.copy(uploadfile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

            String type = filename.substring(filename.lastIndexOf(".") + 1);

            String size = uploadfile.getSize() + "";
            Script script = scriptService.saveScript(filename, aliasName, filepath.toString(), size, type);
            result.put("id", script.getId());
            return ok(result);
        }
        catch (Exception e) {
            log.error(String.format("upload app error %s", uploadfile.getSize()), e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }
    }



    @ResponseBody
    @RequestMapping(value = "/delete", method= RequestMethod.POST, produces={"application/json"})
    public Result delete(@RequestBody Map<String, Object> params){
        List<String> ids;
        try {
            ids = cast(params.getOrDefault("ids", null));
        }catch (Exception e){
            return fail(ResultCode.PARAM_ERROR.getValue(), e.getMessage());
        }
        if (ids == null) {
            return ok();
        }
        for(String id : ids){
            scriptService.deleteById(id);
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
            filterDisable(filters);
            List<Project> projectsOfUser = projectService.findByUser(getCurrentUsername());
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<Script> testwaScripts =  scriptService.find(filters, pageRequest);
            Iterator<Script> testwaScriptsIter =  testwaScripts.iterator();
            List<ScriptVO> lists = new ArrayList<>();
            while(testwaScriptsIter.hasNext()){
                Script ts = testwaScriptsIter.next();
                String appId = ts.getAppId();
                if(StringUtils.isBlank(appId)){
                    log.error("This testcase's appId was not found", ts.toString());
                }
                App app = appService.getAppById(appId);
                if(app == null){
                    log.error("This app: {}, was not found", appId);
                }
                Project project = projectService.findById(app.getProjectId());

                lists.add(new ScriptVO(ts, app, project));
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
    @RequestMapping(value="/{id}", method= RequestMethod.GET)
    public Result readScript(@PathVariable String id){
        Script script = scriptService.getScriptById(id);
        String path = script.getPath();
        StringBuffer sb = new StringBuffer();
        try {
            Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            log.error("Read file error", e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }
        return ok(sb.toString());
    }


    @ResponseBody
    @RequestMapping(value={"/{id}"}, method = { RequestMethod.POST}, produces={"application/json"})
    public Result writeScript(@PathVariable String id,
                              @RequestBody Map<String, String> params){
        Script script = scriptService.getScriptById(id);
        String path = script.getPath();

        String content = params.getOrDefault("content", "");
        if(StringUtils.isBlank(content)){
            log.error("Send content is null");
            return fail(ResultCode.PARAM_ERROR.getValue(), "参数错误");
        }
        try {
            content = replaceBlank(content);
            Path scriptPath = Paths.get(path);
            Path target = Paths.get(System.getProperty("java.io.tmpdir"), env.getProperty("server.context-path").replace("/", ""), String.valueOf(Identities.randomLong()));
            if(Files.notExists(target)){
                Files.createDirectories(target);
            }
            Files.move(scriptPath, target.resolve(scriptPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            byte[] b = content.getBytes(StandardCharsets.UTF_8);
            Files.write(scriptPath, b);

            User user = userService.findByUsername(getCurrentUsername());
            script.setModifyDate(new Date());
            script.setModifyUserId(user.getId());
            script.setModifyUserId(user.getUsername());
        } catch (IOException e) {
            log.error("Write file error", e);
            return fail(ResultCode.SERVER_ERROR.getValue(), e.getMessage());
        }
        return ok();
    }

    /**
     * change tab to blankspace
     * @param str
     * @return string
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\t");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("\u0020\u0020\u0020\u0020");
        }
        return dest;
    }

}
