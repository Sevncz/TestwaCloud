package com.testwa.distest.server.service.testcase.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDetailDAO;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TestcaseService {
    @Autowired
    private ITestcaseDAO testcaseDAO;
    @Autowired
    private ITestcaseDetailDAO testcaseDetailDAO;
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
        testcase.setCaseName(form.getName());
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
        testcase.setCaseName("compatibility test");
        testcase.setDescription("compatibility test");
        testcase.setProjectId(projectId);
        testcase.setCreateBy(user.getId());
        testcase.setTag("兼容");
        testcase.setExeMode(DB.RunMode.COMPATIBILITYTEST);
        return testcaseDAO.insert(testcase);
    }

    private void saveTestcaseScript(List<Long> scriptIds, long testcaseId) {
        List<Object> seq = new ArrayList<>();
        scriptIds.forEach(scriptId -> {
            TestcaseDetail testcaseScript = new TestcaseDetail();
            testcaseScript.setScriptId(scriptId);
            testcaseScript.setSeq(seq.size());
            testcaseScript.setTestcaseId(testcaseId);
            testcaseDetailDAO.insert(testcaseScript);
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
        testcaseDetailDAO.deleteByTestcaseId(testcaseId);
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

    public TestcaseVO getTestcaseVO(Long caseId) {
        Testcase testcase = testcaseDAO.fetchOne(caseId);
        TestcaseVO testcaseVO = new TestcaseVO();
        if(testcase != null){
            toTestcaseVO(testcase, testcaseVO);
        }

        return testcaseVO;
    }

    public void toTestcaseVO(Testcase testcase, TestcaseVO testcaseVO) {
        BeanUtils.copyProperties(testcase, testcaseVO);
        // get scriptVOs
        List<TestcaseDetail> details = testcase.getTestcaseDetails();
        if(details != null && details.size() > 0){
            Collections.sort(details);
            List<ScriptVO> scripts = new ArrayList<>();
            details.forEach( d -> {
                ScriptVO scriptVO = new ScriptVO();
                BeanUtils.copyProperties(d.getScript(), scriptVO);
                scripts.add(scriptVO);
            });
            testcaseVO.setScriptList(scripts);
        }

        // get create user vo
        UserVO createVO = new UserVO();
        User createUser = testcase.getCreateUser();
        if(createUser != null) {
            BeanUtils.copyProperties(createUser, createVO);
            testcaseVO.setCreateUser(createVO);
        }
        // get update user vo
        UserVO updateVO = new UserVO();
        User updateUser = testcase.getUpdateUser();
        if(updateUser != null){
            BeanUtils.copyProperties(updateUser, updateVO);
            testcaseVO.setUpdateUser(updateVO);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(TestcaseUpdateForm form) {
        Testcase testcase = testcaseDAO.findOne(form.getTestcaseId());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        List<Long> scriptIds = form.getScriptIds();
        testcase.setTag(form.getTag());
        testcase.setDescription(form.getDescription());
        testcase.setCaseName(form.getName());
        testcase.setUpdateBy(user.getId());
        testcase.setUpdateTime(new Date());
        testcaseDAO.update(testcase);

        testcaseDetailDAO.deleteByTestcaseId(form.getTestcaseId());
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
        testcase.setCaseName(queryForm.getCaseName());
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
    public Testcase fetchOne(Long entityId) {
        return testcaseDAO.fetchOne(entityId);
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
        List<Project> projects = projectService.findAllByUserList(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        if(queryForm.getProjectId() != null){
            params.put("projectId", queryForm.getProjectId());
        }
        if(StringUtils.isNotEmpty(queryForm.getCaseName())){
            params.put("caseName", queryForm.getCaseName());
        }
        if(projects != null){
            params.put("projects", projects);
        }
        return params;
    }

    public List<Testcase> fetchScriptAllBySceneOrder(Long sceneId) {

        return testcaseDAO.fetchScriptAllBySceneOrder(sceneId);
    }
}