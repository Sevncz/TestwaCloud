package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.exception.NoSuchProjectException;
import com.testwa.distest.server.exception.NoSuchScriptException;
import com.testwa.distest.server.exception.NoSuchTestcaseException;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.repository.ProjectRepository;
import com.testwa.distest.server.mvc.repository.ScriptRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import com.testwa.distest.server.mvc.vo.CreateCaseVO;
import com.testwa.distest.server.mvc.vo.ModifyCaseVO;
import com.testwa.distest.server.mvc.vo.ScriptVO;
import com.testwa.distest.server.mvc.vo.TestcaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
@Transactional
public class TestcaseService extends BaseService {

    @Autowired
    private TestcaseRepository testcaseRepository;
    @Autowired
    private ScriptRepository scriptRepository;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private ProjectRepository projectReporsitory;

    public void save(Testcase testcase) {
        testcaseRepository.save(testcase);
    }

    public void deleteById(String testcaseId) {
        testcaseRepository.delete(testcaseId);
    }

    public Testcase getTestcaseById(String testcaseId) {
        return testcaseRepository.findOne(testcaseId);
    }

    public Page<Testcase> findAll(PageRequest pageRequest) {
        return testcaseRepository.findAll(pageRequest);
    }

    public Page<Testcase> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

    public List<Testcase> find(List<String> projectIds, String name) {
        Query query = buildQuery(projectIds, name);
        return testcaseRepository.find(query);
    }

    public void createCase(CreateCaseVO createCaseVO, User user) throws NoSuchScriptException, NoSuchProjectException {

        List<String> scriptIds = createCaseVO.getScriptIds();
        this.scriptService.checkScripts(scriptIds);
        String projectId = createCaseVO.getProjectId();
        Project project = checkProject(projectId);

        Testcase testcase = new Testcase();
        testcase.setDescription(createCaseVO.getDescription());
        testcase.setScripts(scriptIds);
        testcase.setProjectId(projectId);
        testcase.setProjectName(project.getProjectName());
        testcase.setName(createCaseVO.getName());
        testcase.setUserId(user.getId());
        testcase.setTag(createCaseVO.getTag());
        testcase.setUserName(user.getUsername());
        save(testcase);
    }

    private Project checkProject(String projectId) throws NoSuchProjectException {
        Project project = this.projectReporsitory.findOne(projectId);
        if (project == null) {
            throw new NoSuchProjectException("没有此项目!");
        }
        return project;
    }

    public Page<Testcase> findPage(PageRequest pageRequest, List<String> projectIds, String caseName) {
        Query query = buildQuery(projectIds, caseName);
        return testcaseRepository.find(query, pageRequest);
    }

    public TestcaseVO getTestcaseVO(String caseId) {
        Testcase testcase = testcaseRepository.findOne(caseId);
        TestcaseVO testcaseVO = new TestcaseVO();
        BeanUtils.copyProperties(testcase, testcaseVO);
        // get scriptVOs
        List<Script> scripts = this.scriptService.findScriptList(testcase.getScripts());
        List<ScriptVO> scriptVOs = new ArrayList<>();
        scripts.forEach(script -> {
            ScriptVO scriptVO = new ScriptVO();
            BeanUtils.copyProperties(script, scriptVO);
            scriptVOs.add(scriptVO);
        });
        testcaseVO.setScriptVOs(scriptVOs);

        return testcaseVO;
    }

    public void modifyCase(ModifyCaseVO modifyCaseVO) throws NoSuchTestcaseException, NoSuchScriptException, NoSuchProjectException {
        Testcase testcase = testcaseRepository.findOne(modifyCaseVO.getId());
        if (testcase == null) {
            throw new NoSuchTestcaseException("无此案例！");
        }

        List<String> scriptIds = modifyCaseVO.getScriptIds();
        this.scriptService.checkScripts(scriptIds);

        testcase.setScripts(scriptIds);
        testcase.setTag(modifyCaseVO.getTag());
        testcase.setDescription(modifyCaseVO.getDescription());
        testcase.setName(modifyCaseVO.getName());
        testcaseRepository.save(testcase);
    }

    public List<Script> findList(List<String> projectIds, String name) {

        return null;
    }

    protected void checkTestcases(List<String> caseIds) throws NoSuchTestcaseException {
        List<Testcase> cases = this.findTestcaseList(caseIds);
        if (cases.size() != caseIds.size()) {
            throw new NoSuchTestcaseException("没有此案例!");
        }
    }

    protected List<Testcase> findTestcaseList(List<String> cases) {
        // 修复返回列表不按照顺序的bug 使用逐个获取策略
        List<Testcase> caseList = new ArrayList<>();
        cases.forEach(caseId -> {
            Testcase testcase = testcaseRepository.findOne(caseId);
            caseList.add(testcase);
        });
        return caseList;
    }

    public List<Testcase> getCaseByProjectId(String projectId) {
        return testcaseRepository.findByProjectId(projectId);
    }

    public Integer getCountCaseByProjectId(String projectId) {
        return testcaseRepository.countByProjectId(projectId);
    }
}
