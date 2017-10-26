package com.testwa.distest.server.service.script.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.utils.IOUtil;
import com.testwa.core.utils.Identities;
import com.testwa.core.utils.PinYinTool;
import com.testwa.distest.common.constant.ResultCode;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.entity.Script;
import com.testwa.distest.server.mvc.entity.User;
import com.testwa.distest.server.service.script.dao.IScriptDAO;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptNewForm;
import com.testwa.distest.server.service.script.form.ScriptUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 21/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptService {

    @Autowired
    private IScriptDAO scriptDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private Environment env;

    public Script findOne(Long scriptId){
        return scriptDAO.findOne(scriptId);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void uploadMulti(List<MultipartFile> uploadfiles) throws IOException {
        for(MultipartFile f : uploadfiles){
            upload(f);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Script upload(MultipartFile uploadfile) throws IOException {

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
        Script script = saveScript(filename, aliasName, filepath.toString(), size, type);
        return script;
    }

    private Script saveScript(String filename, String aliasName, String filepath, String size, String type) throws IOException {
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

        script.setCreateTime(new Date());
        script.setScriptName(filename);
        script.setPath(filepath);
        script.setAliasName(aliasName);
        script.setSize(size);
        script.setMd5(IOUtil.fileMD5(filepath));
        scriptDAO.insert(script);
        return script;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void update(ScriptUpdateForm form) {

        Script script = findOne(form.getScriptId());
        User user = userService.findByUsername(getCurrentUsername());
        script.setProjectId(form.getProjectId());
        script.setTag(form.getTag());
        script.setDescription(form.getDescription());
        script.setCreateBy(user.getId());
        script.setCreateTime(new Date());
        scriptDAO.update(script);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void delete(List<Long> entityIds) {
        scriptDAO.delete(entityIds);
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

    public void modifyContent(Long scriptId, String content) throws AccountException, IOException {
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
