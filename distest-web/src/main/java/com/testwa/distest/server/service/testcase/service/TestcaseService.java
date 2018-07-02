package com.testwa.distest.server.service.testcase.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppInfoService;
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
    private AppInfoService appInfoService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    /**
     * 保存回归测试测试案例
     * @param projectId
     * @param appInfo
     * @param form
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long saveHGTestcase(Long projectId, AppInfo appInfo, TestcaseNewForm form) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        Testcase testcase = new Testcase();
        if(StringUtils.isNotEmpty(form.getName())){
            testcase.setCaseName(form.getName());
        }else{
            testcase.setCaseName(String.format("案例-%s", TimeUtil.getTimestampForFile()));
        }
        testcase.setDescription(form.getDescription());
        testcase.setProjectId(projectId);
        testcase.setTag(form.getTag());
        testcase.setAppInfoId(appInfo.getId());
        testcase.setPackageName(appInfo.getPackageName());
        testcase.setAppName(appInfo.getName());
        testcase.setCreateBy(user.getId());
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);
        long testcaseId = testcaseDAO.insert(testcase);
        saveTestcaseScript(form.getScriptIds(), testcaseId);
        return testcaseId;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Testcase saveTestcaseByScriptIds(App app, List<Long> scriptIds) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        AppInfo appInfo = appInfoService.getByPackage(app.getProjectId(), app.getPackageName());
        Testcase testcase = new Testcase();
        testcase.setCaseName(String.format("案例-%s", TimeUtil.getTimestampForFile()));
        testcase.setProjectId(app.getProjectId());
        testcase.setAppInfoId(appInfo.getId());
        testcase.setPackageName(appInfo.getPackageName());
        testcase.setAppName(appInfo.getName());
        testcase.setCreateBy(user.getId());
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);
        long testcaseId = testcaseDAO.insert(testcase);
        saveTestcaseScript(scriptIds, testcaseId);
        testcase.setId(testcaseId);
        return testcase;
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


    public PageResult<Testcase> findPage(Long projectId, TestcaseListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        List<Testcase> testcaseList = findList(projectId, pageForm);
        PageInfo<Testcase> info = new PageInfo(testcaseList);
        PageResult<Testcase> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Testcase> findList(Long projectId, TestcaseListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Testcase testcase = new Testcase();
        if(StringUtils.isNotBlank(queryForm.getCaseName())){
            testcase.setCaseName(queryForm.getCaseName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())){
            testcase.setPackageName(queryForm.getPackageName());
        }
        testcase.setProjectId(projectId);
        return testcaseDAO.findBy(testcase);
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

    public List<Testcase> findByScripts(List<Long> scriptIds) {
        return testcaseDAO.fetchContainsScripts(scriptIds);
    }
}
