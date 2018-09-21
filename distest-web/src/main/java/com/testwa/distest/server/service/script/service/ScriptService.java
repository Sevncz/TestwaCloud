package com.testwa.distest.server.service.script.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.Project;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.script.dao.IScriptDAO;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptNewForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 21/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptService {
    private static final String REG_ANDROID_PACKAGE = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*";
    @Autowired
    private IScriptDAO scriptDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DisFileProperties disFileProperties;
    @Autowired
    private Environment env;

    public Script findOne(Long scriptId){
        return scriptDAO.findOne(scriptId);
    }

    public Script findOneInPorject(Long scriptId, Long projectId){
        return scriptDAO.findOneInPorject(scriptId, projectId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void uploadMulti(List<MultipartFile> uploadfiles) throws IOException {
        for(MultipartFile f : uploadfiles){
            upload(f);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void uploadMulti(List<MultipartFile> uploadfiles, Long projectId) throws IOException {
        for(MultipartFile f : uploadfiles){
            upload(f, projectId);
        }
    }

    /**
     * 只上传脚本文件
     * @param uploadfile
     * @return
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Script upload(MultipartFile uploadfile) throws IOException {
        return upload(uploadfile, null);
    }

    /**
     * 上传到项目中
     * @param uploadfile
     * @param projectId
     * @return
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Script upload(MultipartFile uploadfile, Long projectId) throws IOException {
        // 解析文件
        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        String dirName = Identities.uuid2();
        Path dir = Paths.get(disFileProperties.getScript(), dirName);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.copy(uploadfile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
        String md5 = IOUtil.fileMD5(filepath.toString());

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        String relativePath = dirName + File.separator + aliasName;

        // 保存至数据库
        Script script = new Script();

        switch (type.toLowerCase()){
            case "py":
                script.setLn(DB.ScriptLN.PYTHON);
                // 解析basepackage
                try (Stream<String> stream = Files.lines(filepath, StandardCharsets.UTF_8)) {
                    stream.forEach( l -> {
                        if(StringUtils.isBlank(script.getAppPackage())){
                            if(l.contains("appPackage")) {
                                // desired_caps['appPackage'] ='com.orion.xiaoya.speakerclient'
                                String appPackage = resovlePyPacakge(l);
                                script.setAppPackage(appPackage);
                            }
                        }
                    });
                } catch (IOException e1) {
                    log.error("read error file", e1);
                }
                break;
            case "java":
                script.setLn(DB.ScriptLN.JAVA);
                break;
            case "rb":
                script.setLn(DB.ScriptLN.RUBY);
                break;
            case "js":
                script.setLn(DB.ScriptLN.JS);
                break;
            case "php":
                script.setLn(DB.ScriptLN.PHP);
                break;
            default:
                script.setLn(DB.ScriptLN.UNKNOWN);
                break;
        }

        User currentUser = userService.findByUsername(getCurrentUsername());
        script.setCreateBy(currentUser.getId());
        script.setCreateTime(new Date());
        script.setScriptName(filename);
        script.setPath(relativePath);
        script.setAliasName(aliasName);
        script.setSize(size);
        script.setMd5(md5);
        script.setProjectId(projectId);
        script.setEnabled(true);
        Long scriptId = scriptDAO.insert(script);
        script.setId(scriptId);
        return script;
    }

    public String resovlePyPacakge(String text) {
        String[] items = text.split("=");
        if(items.length == 2) {
            String basepackage = items[1];
            Pattern r = Pattern.compile(REG_ANDROID_PACKAGE);
            Matcher matcher = r.matcher(basepackage);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return null;
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(ScriptUpdateForm form) {

        Script script = findOne(form.getScriptId());
        User currentUser = userService.findByUsername(getCurrentUsername());
        script.setProjectId(form.getProjectId());
        script.setTag(form.getTag());
        script.setDescription(form.getDescription());
        script.setUpdateTime(new Date());
        script.setUpdateBy(currentUser.getId());
        scriptDAO.update(script);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void appendInfo(ScriptUpdateForm form) {

        Script script = findOne(form.getScriptId());
        User currentUser = userService.findByUsername(getCurrentUsername());
        script.setProjectId(form.getProjectId());
        script.setTag(form.getTag());
        script.setDescription(form.getDescription());
        script.setUpdateTime(new Date());
        script.setUpdateBy(currentUser.getId());
        script.setEnabled(true);
        scriptDAO.update(script);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> entityIds) {
        scriptDAO.disableAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long entityId) {
        scriptDAO.disable(entityId);
    }

    public PageResult<Script> findPage(Long projectId, ScriptListForm queryForm) {
        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        List<Script> scriptList = findList(projectId, queryForm);
        PageInfo<Script> info = new PageInfo(scriptList);
        PageResult<Script> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Script> findList(Long projectId, ScriptListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Script script = new Script();
        script.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getScriptName())) {
            script.setScriptName(queryForm.getScriptName());
        }
        if(queryForm.getLn() != null) {
            script.setLn(DB.ScriptLN.valueOf(queryForm.getLn()));
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            script.setAppPackage(queryForm.getPackageName());
        }
        return scriptDAO.findBy(script);
    }


    public List<Script> findList(Long projectId, Long startTime, Long endTime, ScriptListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Script script = new Script();
        script.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getScriptName())) {
            script.setScriptName(queryForm.getScriptName());
        }
        if(queryForm.getLn() != null) {
            script.setLn(DB.ScriptLN.valueOf(queryForm.getLn()));
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            script.setAppPackage(queryForm.getPackageName());
        }
        return scriptDAO.findBy(script, startTime, endTime);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void modifyContent(Long scriptId, String content) throws IOException {
        Script script = findOne(scriptId);
        String path = disFileProperties.getScript() + File.separator + script.getPath();

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
        script.setUpdateTime(new Date());
        script.setUpdateBy(user.getId());

        scriptDAO.update(script);
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

    public List<Script> findAllByTestcaseId(Long caseId) {

        return null;
    }

    public List<Script> findAll(List<Long> scriptIds) {
        return scriptDAO.findAll(scriptIds);
    }

    public List<Script> findAllInProject(List<Long> scriptIds, Long projectId) {
        return scriptDAO.findAllInProject(scriptIds, projectId);
    }

    public String getContent(Long scriptId) throws IOException {
        Script script = findOne(scriptId);
        return getContent(script);
    }

    public String getContent(Script script) throws IOException {
        String path = disFileProperties.getScript() + File.separator + script.getPath();
        StringBuffer sb = new StringBuffer();
        Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        return sb.toString();
    }

    public List<Script> findByMD5InProject(String jrMd5, Long projectId) {
        Script query = new Script();
        query.setMd5(jrMd5);
        query.setProjectId(projectId);
        return scriptDAO.findBy(query);
    }
}
