package com.testwa.distest.server.service.testcase.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.common.exception.NoSuchTestcaseException;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import com.testwa.distest.server.service.testcase.dao.ITestcaseScriptDAO;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 21/10/2017.
 */
@Log4j2
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TestcaseService {
    @Autowired
    private ITestcaseDAO testcaseDAO;
    @Autowired
    private ITestcaseScriptDAO testcaseScriptDAO;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    /**
     * 保存回归测试测试案例
     * @param form
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long saveRegressionTestcase(TestcaseNewForm form) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Testcase testcase = new Testcase();
        testcase.setDescription(form.getDescription());
        testcase.setProjectId(form.getProjectId());
        testcase.setCreateBy(user.getId());
        testcase.setTag(form.getTag());
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);
        testcase.setExeMode(DB.RunMode.REGRESSIONTEST);
        long testcaseId = testcaseDAO.insert(testcase);
        saveTestcaseScript(form.getScriptIds(), testcaseId);
        return testcaseId;
    }

    /**
     * 保存兼容测试测试案例
     * @param projectId
     * @param scripts
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Long saveCompatibilityTestcase(Long projectId, List<Long> scripts){
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Testcase testcase = new Testcase();
        testcase.setDescription("compatibility test");
        testcase.setProjectId(projectId);
        testcase.setCaseName("compatibility test");
        testcase.setCreateBy(user.getId());
        testcase.setTag("兼容");
        testcase.setExeMode(DB.RunMode.COMPATIBILITYTEST);
        return testcaseDAO.insert(testcase);
    }

    private void saveTestcaseScript(List<Long> scriptIds, long testcaseId) {
        List<Object> seq = new ArrayList<>();
        scriptIds.forEach(scriptId -> {
            TestcaseScript testcaseScript = new TestcaseScript();
            testcaseScript.setScriptId(scriptId);
            testcaseScript.setSeq(seq.size());
            testcaseScript.setTestcaseId(testcaseId);
            testcaseScriptDAO.insert(testcaseScript);
            seq.add(1);
        });
    }

    /**
     * 删除案例及案例脚本的中间表
     * @param testcaseId
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long testcaseId) {
        // 删除相关的testcasescript
        testcaseScriptDAO.deleteByTestcaseId(testcaseId);
        testcaseDAO.delete(testcaseId);
    }

    /**
     * 删除多个案例记录
     * @param testcaseIds
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> testcaseIds) {
        testcaseIds.forEach(this::delete);
    }

    public Testcase findOne(String testcaseId) {
        return testcaseDAO.findOne(testcaseId);
    }

    public TestcaseVO getTestcaseVO(Long caseId) {
        Testcase testcase = testcaseDAO.findOne(caseId);
        TestcaseVO testcaseVO = new TestcaseVO();
        BeanUtils.copyProperties(testcase, testcaseVO);
        // get scriptVOs
        List<Script> scripts = scriptService.findAllByTestcaseId(caseId);
        List<ScriptVO> scriptVOs = new ArrayList<>();
        scripts.forEach(script -> {
            ScriptVO scriptVO = new ScriptVO();
            BeanUtils.copyProperties(script, scriptVO);
            scriptVOs.add(scriptVO);
        });
        testcaseVO.setScriptVOs(scriptVOs);

        return testcaseVO;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(TestcaseUpdateForm form) {
        Testcase testcase = testcaseDAO.findOne(form.getTestcaseId());
        if(testcase == null){
            return;
        }
        List<Long> scriptIds = form.getScriptIds();
        testcase.setTag(form.getTag());
        testcase.setDescription(form.getDescription());
        testcase.setCaseName(form.getName());
        testcaseDAO.update(testcase);

        testcaseScriptDAO.deleteByTestcaseId(form.getTestcaseId());
        saveTestcaseScript(scriptIds, form.getTestcaseId());

    }

    /**
     * 按传入ID的顺序返回列表
     * @param cases
     * @return
     */
    public List<Testcase> findByCaseOrder(List<Long> cases) {
        StringBuffer orderSb = new StringBuffer();
        orderSb.append("field(id,");
        String order = Joiner.on(",").join(cases);
        orderSb.append(order).append(")");
        List<Testcase> caseList = testcaseDAO.findAllOrder(cases, orderSb.toString());
        return caseList;
    }

    public long countByProject(Long projectId) {
        Testcase query = new Testcase();
        query.setProjectId(projectId);
        return testcaseDAO.countBy(query);
    }


    public PageResult<Testcase> findPage(TestcaseListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Testcase> testcaseList = find(pageForm);
        PageInfo<Testcase> info = new PageInfo(testcaseList);
        PageResult<Testcase> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Testcase> find(TestcaseListForm queryForm) {
        Testcase testcase = new Testcase();
        testcase.setCaseName(queryForm.getTestcaseName());
        testcase.setProjectId(queryForm.getProjectId());
        List<Testcase> testcaseList = testcaseDAO.findBy(testcase);
        return testcaseList;
    }

    public List<Testcase> findAll(List<Long> entityIds) {
        return testcaseDAO.findAll(entityIds);
    }

    public Testcase findOne(Long entityId) {
        return testcaseDAO.findOne(entityId);
    }

    public PageResult<Testcase> findPageForCurrentUser(TestcaseListForm pageForm) {
        Map<String, Object> params = buildProjectParamsForCurrentUser(pageForm);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());

        List<Testcase> testcaseList = testcaseDAO.findByFromProject(params);
        PageInfo<Testcase> info = new PageInfo(testcaseList);
        PageResult<Testcase> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Testcase> findForCurrentUser(TestcaseListForm queryForm) {
        Map<String, Object> params = buildProjectParamsForCurrentUser(queryForm);
        List<Testcase> testcaseList = testcaseDAO.findByFromProject(params);
        return testcaseList;
    }

    private Map<String, Object> buildProjectParamsForCurrentUser(TestcaseListForm queryForm){
        List<Project> projects = projectService.findAllOfUserProject(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", queryForm.getProjectId());
        params.put("caseName", queryForm.getTestcaseName());
        params.put("projects", projects);
        return params;
    }

}
