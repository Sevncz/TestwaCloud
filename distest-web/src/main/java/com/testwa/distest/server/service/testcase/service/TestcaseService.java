package com.testwa.distest.server.service.testcase.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.common.exception.NoSuchTestcaseException;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.core.entity.*;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wen on 21/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TestcaseService {
    @Autowired
    private ITestcaseDAO testcaseDAO;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    public long save(TestcaseNewForm form) throws AccountException, NoSuchProjectException, NoSuchScriptException {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());


        Testcase testcase = new Testcase();
        testcase.setDescription(form.getDescription());
        testcase.setProjectId(form.getProjectId());
        testcase.setCreateBy(user.getId());
        testcase.setTag(form.getTag());
        testcase.setCreateTime(new Date());
        long testcaseId = testcaseDAO.insert(testcase);
        return testcaseId;
    }

    public void delete(Long testcaseId) {
        testcaseDAO.delete(testcaseId);
    }
    public void delete(List<Long> testcaseIds) {
        testcaseDAO.delete(testcaseIds);
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

    public void modifyCase(TestcaseUpdateForm form) throws NoSuchTestcaseException, NoSuchScriptException, NoSuchProjectException {
        Testcase testcase = testcaseDAO.findOne(form.getTestcaseId());
        if (testcase == null) {
            throw new NoSuchTestcaseException("无此案例！");
        }

        List<Long> scriptIds = form.getScriptIds();

//        testcase.setScripts(scriptIds);
        testcase.setTag(form.getTag());
        testcase.setDescription(form.getDescription());
        testcase.setCaseName(form.getName());
        testcaseDAO.update(testcase);
    }

    protected void checkTestcases(List<Long> caseIds) throws NoSuchTestcaseException {
        List<Testcase> cases = findAllOrder(caseIds);
        if (cases.size() != caseIds.size()) {
            throw new NoSuchTestcaseException("没有此案例!");
        }
    }

    /**
     * 按传入ID的顺序返回列表
     * @param cases
     * @return
     */
    public List<Testcase> findAllOrder(List<Long> cases) {
        // 修复返回列表不按照顺序的bug 使用逐个获取策略
        StringBuffer orderSb = new StringBuffer();
        orderSb.append("field(");
        String order = Joiner.on(",").join(cases);
        orderSb.append(order).append(")");
        List<Testcase> caseList = testcaseDAO.findAllOrder(cases, orderSb.toString());
        return caseList;
    }

    public List<Testcase> findByProject(Long projectId) {
        Testcase query = new Testcase();
        query.setProjectId(projectId);
        return testcaseDAO.findBy(query);
    }

    public long countByProject(Long projectId) {
        Testcase query = new Testcase();
        query.setProjectId(projectId);
        return testcaseDAO.countBy(query);
    }

    public Long createCaseQuick(Long projectId, List<Long> scripts) throws NoSuchScriptException, NoSuchProjectException, AccountException {
//        scriptService.validateScripts(scripts);
        Project project = projectService.findOne(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        Testcase testcase = new Testcase();
        testcase.setDescription("quick execute");
        testcase.setProjectId(projectId);
        testcase.setCaseName("quick execute");
        testcase.setCreateBy(user.getId());
        testcase.setTag("quick execute");
        return testcaseDAO.insert(testcase);
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
}
