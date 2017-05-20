package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaProject;
import com.testwa.distest.server.model.TestwaReportDetail;
import com.testwa.distest.server.model.User;
import com.testwa.distest.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class TestwaProjectService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(TestwaProjectService.class);

    @Autowired
    private TestwaProjectRepository projectRepository;
    @Autowired
    private TestwaTestcaseRepository testwaTestcaseRepository;
    @Autowired
    private TestwaAppRepository testwaAppRepository;
    @Autowired
    private TestwaScriptRepository testwaScriptRepository;
    @Autowired
    private TestwaReportRepository testwaReportRepository;

    public void save(TestwaProject testwaProject){
        projectRepository.save(testwaProject);
    }

    public void deleteById(String projectId){
        // 物理删除改成软删除
//        projectRepository.delete(projectId);
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(projectId));

        Update update = new Update();
        update.set("disable", false);
        update.set("modifyDate", new Date());
        projectRepository.updateMulti(query, update);

        deleteAllRelatedObjectByProjectId(projectId);
    }

    public void deleteAllRelatedObjectByProjectId(String projectId){

        Query query = new Query();
        query.addCriteria(Criteria.where("projectId").is(projectId));

        Update update = new Update();
        update.set("disable", false);
        update.set("modifyDate", new Date());

        testwaAppRepository.updateMulti(query, update);
        testwaScriptRepository.updateMulti(query, update);
        testwaTestcaseRepository.updateMulti(query, update);
        testwaReportRepository.updateMulti(query, update);
    }

    public TestwaProject getProjectById(String projectId){
        return projectRepository.findOne(projectId);
    }

    public Page<TestwaProject> findAll(PageRequest pageRequest) {
        return projectRepository.findAll(pageRequest);
    }

    public List<TestwaProject> findAll() {
        return projectRepository.findAll();
    }

    public Page<TestwaProject> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return projectRepository.find(query, pageRequest);
    }

    public List<TestwaProject> find(List<Map<String, String>> filters) {
        Query query = buildQuery(filters);
        return projectRepository.find(query);
    }

    public void addMember(String projectId, User user) {

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(projectId));

        Update update = new Update();
        update.addToSet("members", user.getUsername());

        projectRepository.updateMulti(query, update);
    }

    public void delMember(String projectId, User user) {

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(projectId));

        Update update = new Update();
        update.pull("members", user.getUsername());

        projectRepository.updateMulti(query, update);
    }

    /**
     * 获得用户创建的和用户参加的所有project
     * @param user
     * @return
     */
    public List<TestwaProject> findByUser(User user) {
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("userId").is(user.getId()), Criteria.where("members").in(user.getUsername()));
        criteria.andOperator(Criteria.where("disable").is(true));
        Query query = new Query(criteria);
        return projectRepository.find(query);
    }

    public TestwaProject findById(String projectId) {
        return projectRepository.findById(projectId);
    }
}
