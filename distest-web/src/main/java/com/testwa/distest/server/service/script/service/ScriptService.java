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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 21/10/2017.
 */
@Log4j2
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptService {

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

    /**
     * 只上传脚本文件
     * @param uploadfile
     * @return
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Script upload(MultipartFile uploadfile) throws IOException {

        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        String dirName = Identities.uuid2();
        Path dir = Paths.get(disFileProperties.getScript(), dirName);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.copy(uploadfile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        String relativePath = dirName + File.separator + aliasName;
        Script script = saveScript(filename, aliasName, filepath.toString(), relativePath, size, type, null);
        return script;
    }

    /**
     * 上传文件+记录更新
     * @param uploadfile
     * @param form
     * @return
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Script upload(MultipartFile uploadfile, ScriptNewForm form) throws IOException {

        String filename = uploadfile.getOriginalFilename();
        String aliasName = PinYinTool.getPingYin(filename);
        String dirName = Identities.uuid2();
        Path dir = Paths.get(disFileProperties.getScript(), dirName);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path filepath = Paths.get(dir.toString(), aliasName);
        Files.copy(uploadfile.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        String type = filename.substring(filename.lastIndexOf(".") + 1);

        String size = uploadfile.getSize() + "";
        String relativePath = dirName + File.separator + aliasName;
        Script script = saveScript(filename, aliasName, filepath.toString(), relativePath, size, type, form);
        return script;
    }

    private Script saveScript(String filename, String aliasName, String filepath, String relativePath, String size, String type, ScriptNewForm form) throws IOException {
        Script script = new Script();

        switch (type.toLowerCase()){
            case "py":
                script.setLn(DB.ScriptLN.PYTHON);
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
        script.setMd5(IOUtil.fileMD5(filepath));
        if(form != null){
            script.setProjectId(form.getProjectId());
            script.setTag(form.getTag());
            script.setDescription(form.getDescription());
            script.setEnabled(true);
        }
        Long scriptId = scriptDAO.insert(script);
        script.setId(scriptId);
        return script;
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
        scriptDAO.delete(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long entityId) {
        scriptDAO.delete(entityId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteScript(Long entityId) {

        Script script = findOne(entityId);
        if (script == null){
            return;
        }

        String filePath = script.getPath();
        try {
            // 删除app文件
            Files.deleteIfExists(Paths.get(filePath));
            // 删除app文件的文件夹
            Files.deleteIfExists(Paths.get(filePath).getParent());
        } catch (IOException e) {
            log.error("delete app file error", e);
        }
        // 删除记录
        delete(entityId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteScript(List<Long> entityIds) {
        entityIds.forEach(this::deleteScript);
    }

    public PageResult<Script> findPage(ScriptListForm queryForm) {

        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        List<Script> scriptList = find(queryForm);
        PageInfo<Script> info = new PageInfo(scriptList);
        PageResult<Script> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Script> find(ScriptListForm queryForm) {
        Script script = new Script();
        script.setScriptName(queryForm.getScriptName());
        script.setProjectId(queryForm.getProjectId());
        script.setLn(DB.ScriptLN.valueOf(queryForm.getLn()));
        List<Script> scriptList = scriptDAO.findBy(script);
        return scriptList;
    }

    public PageResult<Script> findPageForCurrentUser(ScriptListForm queryForm) {

        Map<String, Object> params = buildProjectParamsForCurrentUser(queryForm);
        PageHelper.startPage(queryForm.getPageNo(), queryForm.getPageSize());
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        List<Script> scriptList = scriptDAO.findByFromProject(params);
        PageInfo<Script> info = new PageInfo(scriptList);
        PageResult<Script> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Script> findForCurrentUser(ScriptListForm queryForm) {
        Map<String, Object> params = buildProjectParamsForCurrentUser(queryForm);
        List<Script> scriptList = scriptDAO.findByFromProject(params);
        return scriptList;
    }

    private Map<String, Object> buildProjectParamsForCurrentUser(ScriptListForm queryForm){
        List<Project> projects = projectService.findAllByUserList(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        if(StringUtils.isNotEmpty(queryForm.getScriptName())){
            params.put("scriptName", queryForm.getScriptName());
        }
        if(queryForm.getProjectId() != null){
            params.put("projectId", queryForm.getProjectId());
        }
        if(queryForm.getLn() != null){
            params.put("ln", queryForm.getLn());
        }
        if(projects != null & projects.size() > 0){
            params.put("projects", projects);
        }
        return params;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void modifyContent(Long scriptId, String content) throws IOException {
        Script script = findOne(scriptId);
        String path = script.getPath();

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
        String path = script.getPath();
        StringBuffer sb = new StringBuffer();
        Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        return sb.toString();
    }

}
