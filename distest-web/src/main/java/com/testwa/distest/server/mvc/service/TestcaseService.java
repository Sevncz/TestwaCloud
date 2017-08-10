package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.exception.NoSuchProjectException;
import com.testwa.distest.server.exception.NoSuchScriptException;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.repository.ProjectRepository;
import com.testwa.distest.server.mvc.repository.ScriptRepository;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import com.testwa.distest.server.mvc.vo.CreateCaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
@Transactional
public class TestcaseService extends BaseService{

    @Autowired
    private TestcaseRepository testcaseRepository;
    @Autowired
    private ScriptRepository scriptRepository;
    @Autowired
    private ProjectRepository projectReporsitory;

    public void save(Testcase testcase){
        testcaseRepository.save(testcase);
    }

    public void deleteById(String testcaseId){
        testcaseRepository.delete(testcaseId);
    }

    public Testcase getTestcaseById(String testcaseId){
        return testcaseRepository.findOne(testcaseId);
    }

    public Page<Testcase> findAll(PageRequest pageRequest) {
        return testcaseRepository.findAll(pageRequest);
    }

    public Page<Testcase> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return testcaseRepository.find(query, pageRequest);
    }

    public void createCase(CreateCaseVO createCaseVO, User user) throws NoSuchScriptException, NoSuchProjectException {
        List<String> scriptIds = createCaseVO.getScriptIds();
        List<Script> scripts = this.scriptRepository.findByIdNotIn(scriptIds);
        if (scripts.size() > 0) {
            throw new NoSuchScriptException("没有此脚本!");
        }
        String projectId = createCaseVO.getProjectId();
        Project project = this.projectReporsitory.findOne(projectId);
        if (project == null) {
            throw new NoSuchProjectException("没有此项目!");
        }
        Testcase testcase = new Testcase();
        testcase.setScripts(scriptIds);
        testcase.setProjectId(projectId);
        testcase.setProjectName(project.getProjectName());
        testcase.setName(createCaseVO.getName());
        testcase.setUserId(user.getId());
        testcase.setUserName(user.getUsername());
        save(testcase);
    }
}
