package com.testwa.distest.server.service.testcase.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.core.base.service.BaseService;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.condition.TestcaseCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.TestcaseDetailMapper;
import com.testwa.distest.server.mapper.TestcaseMapper;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
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
import java.util.stream.Collectors;

/**
 * Created by wen on 21/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TestcaseService extends BaseService<Testcase, Long> {
    @Autowired
    private TestcaseMapper testcaseMapper;
    @Autowired
    private TestcaseDetailMapper testcaseDetailMapper;
    @Autowired
    private AppInfoService appInfoService;
    @Autowired
    private User currentUser;

    /**
     * 保存回归测试测试案例
     * @param projectId
     * @param appInfo
     * @param form
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public long saveFunctionalTestcase(Long projectId, AppInfo appInfo, TestcaseNewForm form) {
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
        testcase.setCreateBy(currentUser.getId());
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);
        testcaseMapper.insert(testcase);
        saveTestcaseScript(form.getScriptIds(), testcase.getId());
        return testcase.getId();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Testcase saveTestcaseByScriptIds(App app, List<Long> scriptIds) {
        AppInfo appInfo = appInfoService.getByPackage(app.getProjectId(), app.getPackageName());
        Testcase testcase = new Testcase();
        testcase.setCaseName(String.format("案例-%s", TimeUtil.getTimestampForFile()));
        testcase.setProjectId(app.getProjectId());
        testcase.setAppInfoId(appInfo.getId());
        testcase.setPackageName(appInfo.getPackageName());
        testcase.setAppName(appInfo.getName());
        testcase.setCreateBy(currentUser.getId());
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);
        testcaseMapper.insert(testcase);
        saveTestcaseScript(scriptIds, testcase.getId());
        testcase.setId(testcase.getId());
        return testcase;
    }

    private void saveTestcaseScript(List<Long> scriptIds, long testcaseId) {
        List<Object> seq = new ArrayList<>();
        scriptIds.forEach(scriptId -> {
            TestcaseDetail testcaseScript = new TestcaseDetail();
            testcaseScript.setScriptId(scriptId);
            testcaseScript.setSeq(seq.size());
            testcaseScript.setTestcaseId(testcaseId);
            testcaseDetailMapper.insert(testcaseScript);
            seq.add(1);
        });
    }

    /**
     * 删除案例及案例脚本的中间表
     * @param testcaseId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int delete(Long testcaseId) {
        // 删除相关的testcasescript
        testcaseDetailMapper.deleteByTestcaseId(testcaseId);
        return testcaseMapper.delete(testcaseId);
    }

    /**
     * 删除多个案例记录
     * @param testcaseIds
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<Long> testcaseIds) {
        testcaseIds.forEach(this::delete);
    }

    public TestcaseVO getTestcaseVO(Long caseId) {
        Testcase testcase = testcaseMapper.fetchOne(caseId);
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
        if(details != null && !details.isEmpty()){
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

    @Transactional(propagation = Propagation.REQUIRED)
    public void update(TestcaseUpdateForm form) {
        Testcase testcase = get(form.getTestcaseId());
        List<Long> scriptIds = form.getScriptIds();
        testcase.setTag(form.getTag());
        testcase.setDescription(form.getDescription());
        testcase.setCaseName(form.getName());
        testcase.setUpdateBy(currentUser.getId());
        testcase.setUpdateTime(new Date());
        testcaseMapper.update(testcase);

        testcaseDetailMapper.deleteByTestcaseId(form.getTestcaseId());
        saveTestcaseScript(scriptIds, form.getTestcaseId());

    }

    /**
     * 按传入ID的顺序返回列表
     * @param cases
     * @return
     */
    public List<Testcase> findByCaseOrder(List<Long> cases) {
        StringBuilder orderSb = new StringBuilder();
        orderSb.append("field(id,");
        String order = Joiner.on(",").join(cases);
        orderSb.append(order).append(")");
        return testcaseMapper.findAllOrder(cases, orderSb.toString());
    }

    public long countByProject(Long projectId) {
        Testcase query = new Testcase();
        query.setProjectId(projectId);
        return testcaseMapper.countBy(query);
    }


    public PageResult<Testcase> findPage(Long projectId, TestcaseListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        List<Testcase> testcaseList = findList(projectId, pageForm);
        PageInfo<Testcase> info = new PageInfo(testcaseList);
        return new PageResult<>(info.getList(), info.getTotal());
    }

    public List<Testcase> findList(Long projectId, TestcaseListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        TestcaseCondition query = new TestcaseCondition();
        if(StringUtils.isNotBlank(queryForm.getCaseName())){
            query.setCaseName(queryForm.getCaseName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())){
            query.setPackageName(queryForm.getPackageName());
        }
        query.setProjectId(projectId);
        return testcaseMapper.selectByCondition(query);
    }


    public List<Testcase> findAll(List<Long> entityIds) {
        return entityIds.stream().map(this::get).collect(Collectors.toList());
    }

    public Testcase fetchOne(Long entityId) {
        return testcaseMapper.fetchOne(entityId);
    }

    public List<Testcase> findByScripts(List<Long> scriptIds) {
        return testcaseMapper.fetchContainsScripts(scriptIds);
    }
}
