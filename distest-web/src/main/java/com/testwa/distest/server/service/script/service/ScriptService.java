package com.testwa.distest.server.service.script.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.ScriptCondition;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ScriptMapper;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by wen on 21/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptService extends BaseService<Script, Long> {
    private static final String REG_ANDROID_PACKAGE = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*";
    @Autowired
    private ScriptMapper scriptMapper;
    @Autowired
    private DisFileProperties disFileProperties;
    @Autowired
    private Environment env;
    @Autowired
    private User currentUser;

    public Script findOne(Long scriptId){
        Script script = scriptMapper.selectById(scriptId);
        if(script == null) {
            return null;
        }
        return script.getEnabled() ? script : null;
    }

    public Script findOneInPorject(Long scriptId, Long projectId){
        return scriptMapper.findOneInProject(scriptId, projectId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadMulti(List<MultipartFile> uploadfiles) throws IOException {
        for(MultipartFile f : uploadfiles){
            upload(f);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
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

        script.setCreateBy(currentUser.getId());
        script.setCreateTime(new Date());
        script.setScriptName(filename);
        script.setPath(relativePath);
        script.setAliasName(aliasName);
        script.setSize(size);
        script.setMd5(md5);
        script.setProjectId(projectId);
        script.setEnabled(true);
        scriptMapper.insert(script);
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


    @Transactional(propagation = Propagation.REQUIRED)
    public void update(ScriptUpdateForm form) {

        Script script = findOne(form.getScriptId());
        if(script == null) {
            return;
        }
        script.setProjectId(form.getProjectId());
        script.setTag(form.getTag());
        script.setDescription(form.getDescription());
        script.setUpdateTime(new Date());
        script.setUpdateBy(currentUser.getId());
        scriptMapper.update(script);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void appendInfo(ScriptUpdateForm form) {

        Script script = findOne(form.getScriptId());
        if(script == null) {
            return;
        }
        script.setProjectId(form.getProjectId());
        script.setTag(form.getTag());
        script.setDescription(form.getDescription());
        script.setUpdateTime(new Date());
        script.setUpdateBy(currentUser.getId());
        script.setEnabled(true);
        scriptMapper.update(script);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<Long> entityIds) {
        scriptMapper.disableAll(entityIds);
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
        ScriptCondition query = new ScriptCondition();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getScriptName())) {
            query.setScriptName(queryForm.getScriptName());
        }
        if(queryForm.getLn() != null) {
            DB.ScriptLN ln = DB.ScriptLN.valueOf(queryForm.getLn());
            if(ln == null) {
                throw new BusinessException(ResultCode.INVALID_PARAM, "非法参数");
            }
            query.setLn(ln);
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            query.setAppPackage(queryForm.getPackageName());
        }
        return scriptMapper.selectByCondition(query);
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
        return scriptMapper.findBy(script, startTime, endTime);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void modifyContent(Long scriptId, String content) throws IOException {
        Script script = findOne(scriptId);
        if(script == null) {
            return;
        }
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

        script.setUpdateTime(new Date());
        script.setUpdateBy(currentUser.getId());

        scriptMapper.update(script);
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
        return scriptIds.stream().map(this::findOne).collect(Collectors.toList());
    }

    public List<Script> findAllInProject(List<Long> scriptIds, Long projectId) {
        return scriptMapper.findList(scriptIds, projectId, null);
    }

    public String getContent(Long scriptId) throws IOException {
        Script script = findOne(scriptId);
        if(script == null) {
            return "";
        }
        return getContent(script);
    }

    public String getContent(Script script) throws IOException {
        String path = disFileProperties.getScript() + File.separator + script.getPath();
        StringBuffer sb = new StringBuffer();
        Files.lines(Paths.get(path)).forEach(line -> sb.append(line).append("\n"));
        return sb.toString();
    }

    public List<Script> findByMD5InProject(String jrMd5, Long projectId) {
        ScriptCondition query = new ScriptCondition();
        query.setMd5(jrMd5);
        query.setProjectId(projectId);
        return scriptMapper.selectByCondition(query);
    }
}
