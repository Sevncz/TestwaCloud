package com.testwa.distest.server.web;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.authorization.annotation.Authorization;
import com.testwa.distest.server.authorization.annotation.CurrentUser;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.TestwaScript;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.model.params.QueryTableFilterParams;
import com.testwa.distest.server.service.TestwaAppService;
import com.testwa.distest.server.service.TestwaProjectService;
import com.testwa.distest.server.service.TestwaScriptService;
import com.testwa.distest.server.web.VO.ScriptVO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

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
    private TestwaScriptService testwaScriptService;
    @Autowired
    private TestwaAppService testwaAppService;
    @Autowired
    private TestwaProjectService testwaProjectService;

    @Autowired
    private Environment env;

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/save", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> save(@RequestBody Map<String, String> params, @ApiIgnore @CurrentUser User user){
        String appId = params.getOrDefault("appId", "");
        String id = params.getOrDefault("id", "");
        if(StringUtils.isBlank(appId)
                || StringUtils.isBlank(id)){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数错误"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaScript script = testwaScriptService.getScriptById(id);
        if(script == null){
            log.error("ScriptId get script was null", id);
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(),"ScriptId找不到"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TestwaApp app = testwaAppService.getAppById(appId);
        if(app == null){
            log.error("AppId get app was null", appId);
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(),"app不存在"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        script.setAppId(appId);
        script.setProjectId(app.getProjectId());
        script.setDisable(true);
        script.setUserId(user.getId());
        script.setUsername(user.getUsername());
        testwaScriptService.save(script);
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }


    @Authorization
    @ResponseBody
    @RequestMapping(value="/upload-script", method= RequestMethod.POST)
    public ResponseEntity<ResultInfo> upload(@RequestParam("file") MultipartFile uploadfile){
        Map<String, String> result = new HashMap<>();
        if(uploadfile.isEmpty()){
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "文件为空"), HttpStatus.INTERNAL_SERVER_ERROR);
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
            TestwaScript script = testwaScriptService.saveScript(filename, aliasName, filepath.toString(), size, type);
            result.put("id", script.getId());
            return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
        }
        catch (Exception e) {
            log.error(String.format("upload app error %s", uploadfile.getSize()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ids == null) {
            return new ResponseEntity<>(successInfo(), HttpStatus.OK);
        }
        for(String id : ids){
            testwaScriptService.deleteById(id);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.OK);
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value = "/table", method= RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<ResultInfo> tableList(@RequestBody QueryTableFilterParams filter, @ApiIgnore @CurrentUser User user){
        Map<String, Object> result = new HashMap<>();
        try{

            PageRequest pageRequest = buildPageRequest(filter);
            // contains, startwith, endwith
            List filters = filter.filters;
            filterDisable(filters);
            List<TestwaProject> projectsOfUser = testwaProjectService.findByUser(user);
            List<String> projectIds = new ArrayList<>();
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
            filters = filterProject(filters, "projectId", projectIds);
            Page<TestwaScript> testwaScripts =  testwaScriptService.find(filters, pageRequest);
            Iterator<TestwaScript> testwaScriptsIter =  testwaScripts.iterator();
            List<ScriptVO> lists = new ArrayList<>();
            while(testwaScriptsIter.hasNext()){
                TestwaScript ts = testwaScriptsIter.next();
                String appId = ts.getAppId();
                if(StringUtils.isBlank(appId)){
                    log.error("This testcase's appId was not found", ts.toString());
                }
                TestwaApp app = testwaAppService.getAppById(appId);
                if(app == null){
                    log.error("This app: {}, was not found", appId);
                }
                TestwaProject project = testwaProjectService.findById(app.getProjectId());

                lists.add(new ScriptVO(ts, app, project));
            }
            result.put("records", lists);
            result.put("totalRecords", testwaScripts.getTotalElements());
            return new ResponseEntity<>(dataInfo(result), HttpStatus.OK);
        }catch (Exception e){
            log.error(String.format("Get scripts table error, %s", filter.toString()), e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Authorization
    @ResponseBody
    @RequestMapping(value="/{id}", method= RequestMethod.GET)
    public ResponseEntity<ResultInfo> readScript(@PathVariable String id){
        TestwaScript script = testwaScriptService.getScriptById(id);
        String path = script.getPath();
        StringBuffer sb = new StringBuffer();
        try {
            Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            log.error("Read file error", e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(dataInfo(sb.toString()), HttpStatus.OK);
    }

    @Authorization
    @ResponseBody
    @RequestMapping(value={"/{id}"}, method = { RequestMethod.POST}, produces={"application/json"})
    public ResponseEntity<ResultInfo> writeScript(@PathVariable String id,
                                                  @RequestBody Map<String, String> params,
                                                  @ApiIgnore @CurrentUser User user){
        TestwaScript script = testwaScriptService.getScriptById(id);
        String path = script.getPath();

        String content = params.getOrDefault("content", "");
        if(StringUtils.isBlank(content)){
            log.error("Send content is null");
            return new ResponseEntity<>(errorInfo(ResultCode.PARAM_ERROR.getValue(), "参数错误"), HttpStatus.INTERNAL_SERVER_ERROR);
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

            script.setModifyDate(new Date());
            script.setModifyUserId(user.getId());
            script.setModifyUserId(user.getUsername());
        } catch (IOException e) {
            log.error("Write file error", e);
            return new ResponseEntity<>(errorInfo(ResultCode.SERVER_ERROR.getValue(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(successInfo(), HttpStatus.CREATED);
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
