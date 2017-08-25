package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.server.exception.NotInProjectException;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.AppService;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.ScriptService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.CreateAppVO;
import com.testwa.distest.server.mvc.vo.DeleteVO;
import com.testwa.distest.server.mvc.vo.ModifyScriptVO;
import com.testwa.distest.server.mvc.vo.ScriptVO;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wen on 16/9/2.
 */
@Api("脚本相关api")
@RestController
@RequestMapping(path = "/api/script")
public class ScriptController extends BaseController {
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
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = {"application/json"})
    public Result save(@Valid @RequestBody CreateAppVO createAppDTO) {
        String projectId = createAppDTO.getProjectId();
        String id = createAppDTO.getId();
        Script script = scriptService.getScriptById(id);
        User user = userService.findByUsername(getCurrentUsername());
        script.setProjectId(projectId);
        script.setDisable(false);
        script.setUserId(user.getId());
        script.setUsername(user.getUsername());
        scriptService.save(script);
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Result upload(@RequestParam("file") MultipartFile uploadfile) {
        Map<String, String> result = new HashMap<>();
        if (uploadfile.isEmpty()) {
            return fail(ResultCode.PARAM_ERROR, "文件为空");
        }

        try {
            String filename = uploadfile.getOriginalFilename();
            String aliasName = PinYinTool.getPingYin(filename);
            Path dir = Paths.get(env.getProperty("script.save.path"), Identities.uuid2());
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path filepath = Paths.get(dir.toString(), aliasName);
            Files.copy(uploadfile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

            String type = filename.substring(filename.lastIndexOf(".") + 1);

            String size = uploadfile.getSize() + "";
            Script script = scriptService.saveScript(filename, aliasName, filepath.toString(), size, type);
            result.put("id", script.getId());
            result.put("size",script.getSize());
            result.put("type",script.getType());
            return ok(result);
        } catch (Exception e) {
            log.error(String.format("upload app error %s", uploadfile.getSize()), e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }
    }


    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = {"application/json"})
    public Result delete(@Valid @RequestBody DeleteVO deleteVO) {
        for (String id : deleteVO.getIds()) {
            scriptService.deleteById(id);
        }
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(@RequestParam(value = "page") Integer page,
                            @RequestParam(value = "size") Integer size,
                            @RequestParam(value = "sortField") String sortField,
                            @RequestParam(value = "sortOrder") String sortOrder,
                            @RequestParam(required = false) String projectId,
                            @RequestParam(required = false) String scriptName) throws NotInProjectException {
        PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = getProjectIds(projectService, user, projectId);
        Page<Script> scripts = scriptService.findPage(pageRequest, projectIds, scriptName);
        List<ScriptVO> lists = getScriptVOsFromScripts(scripts.getContent());
        PageResult<ScriptVO> pr = new PageResult<>(lists, scripts.getTotalElements());
        return ok(pr);
    }


    @ResponseBody
    @GetMapping(value = "/{id}")
    public Result readScript(@PathVariable String id) {
        Script script = scriptService.getScriptById(id);
        String path = script.getPath();
        StringBuffer sb = new StringBuffer();
        try {
            Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            log.error("Read file error", e);
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }
        return ok(sb.toString());
    }


    @ResponseBody
    @PostMapping(value = {"/{id}"})
    public Result writeScript(@PathVariable String id,
                              @RequestBody ModifyScriptVO modifyScriptVO) {
        Script script = scriptService.getScriptById(id);
        String path = script.getPath();

        String content = modifyScriptVO.getContent();
        try {
            content = replaceBlank(content);
            Path scriptPath = Paths.get(path);
            Path target = Paths.get(System.getProperty("java.io.tmpdir"), env.getProperty("server.context-path").replace("/", ""), String.valueOf(Identities.randomLong()));
            if (Files.notExists(target)) {
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
            return fail(ResultCode.SERVER_ERROR, e.getMessage());
        }
        return ok();
    }

    @ResponseBody
    @GetMapping(value = "/list")
    public Result list(@RequestParam(required=false) String projectId,
                       @RequestParam(required=false) String name) throws NotInProjectException{
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = getProjectIds(projectService, user, projectId);
        List<Script> scripts = scriptService.find(projectIds, name);
        List<ScriptVO> lists = getScriptVOsFromScripts(scripts);
        return ok(lists);
    }

    private List<ScriptVO> getScriptVOsFromScripts(List<Script> scripts) {
        List<ScriptVO> lists = new ArrayList<>();
        scripts.forEach(script -> {
            ScriptVO scriptVO = getScriptVOFromScript(script);
            lists.add(scriptVO);
        });
        return lists;
    }

    private ScriptVO getScriptVOFromScript(Script script) {
        ScriptVO scriptVO = new ScriptVO();
        BeanUtils.copyProperties(script, scriptVO);
        return scriptVO;
    }

    /**
     * change tab to blankspace
     *
     * @param str
     * @return string
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\t");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("\u0020\u0020\u0020\u0020");
        }
        return dest;
    }

}
