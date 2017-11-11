package com.testwa.distest.server.service.project.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.distest.common.exception.*;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.dao.IProjectDAO;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.vo.ProjectStats;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.*;

/**
 * Created by wen on 16/9/1.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private IProjectDAO projectDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private ViewMgr viewMgr;
    @Autowired
    private ProjectMemberService projectMemberService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Long insert(Project entity) {
        entity.setCreateTime(new Date());
        Long projectId =  projectDAO.insert(entity);
        projectMemberService.saveProjectOwner(projectId, entity.getCreateBy());
        return projectId;
    }

    /**
     * 返回删除的项目数量
     * @param projectId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(Long projectId) {
        projectMemberService.delAllMembers(projectId);
        return projectDAO.delete(projectId);
    }

    /**
     * 返回删除的项目数量
     * @param projectIds
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int deleteAll(List<Long> projectIds) {
        projectIds.forEach( id -> {
            projectMemberService.delAllMembers(id);
        });
        return projectDAO.delete(projectIds);
    }

    public List<Project> findAll() {
        return projectDAO.findAll();
    }


    public Project findOne(Long projectId) {
        return projectDAO.findOne(projectId);
    }


    public List<Project> findAll(List<Long> projectIds) {
        return projectDAO.findAll(projectIds);
    }


    public long getProjectCountByOwner(Long userId) {
        Project query = new Project();
        query.setCreateBy(userId);
        return projectDAO.count(query);
    }

    public List<Project> getRecentViewProject(String username) throws Exception {
        return viewMgr.getRecentViewProject(username);
    }

    public ProjectStats getProjectStats(String projectId, User user) {
        // get available device count
//        Integer devices = remoteClientService.getDeviceByUserIdAndProjectId(user.getId(), projectId).size();
        // apps
//        Integer apps = appService.getCountAppByProjectId(projectId);
        // scripts
//        Integer scripts = scriptService.getCountScriptByProjectId(projectId);
        // cases
//        Integer cases = testcaseService.getCountCaseByProjectId(projectId);
        // tasks
//        Integer tasks = taskService.getCountTaskByProjectId(projectId);
        // todo: reports
//        Integer reports =
//        return new ProjectStats(devices, apps, scripts, cases, tasks, 0);
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Project save(ProjectNewForm form) throws AccountNotFoundException, AccountException, AuthorizedException, AccountAlreadyExistException, NoSuchProjectException, ParamsException {

        User currentUser = userService.findByUsername(WebUtil.getCurrentUsername());

        Project project = new Project();
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setCreateBy(currentUser.getId());
        Long projectId = insert(project);
        project.setId(projectId);
        // 保存成员
        MembersModifyForm membersModifyForm = new MembersModifyForm();
        membersModifyForm.setProjectId(projectId);
        membersModifyForm.setUsernames(form.getMembers());
        projectMemberService.addMembers(membersModifyForm);
        return project;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Project update(ProjectUpdateForm form) throws AccountNotFoundException, AccountException, NoSuchProjectException, AuthorizedException, ParamsException, AccountAlreadyExistException {

        User currentUser = userService.findByUsername(WebUtil.getCurrentUsername());
        Project project = projectDAO.findOne(form.getProjectId());
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setUpdateTime(new Date());
        project.setUpdateBy(currentUser.getId());
        projectDAO.update(project);

        List<User> members = projectMemberService.findAllMembers(project.getId());
        // TODO 1. 计算members和users的差

        // TODO 2. 删除members中存在而users不存在的

        MembersModifyForm membersModifyForm = new MembersModifyForm();
        membersModifyForm.setProjectId(form.getProjectId());
        membersModifyForm.setUsernames(form.getMembers());
        projectMemberService.addMembers(membersModifyForm);
        return project;
    }

    public List<Project> findAllOfUserProject(String username) {
        User user = userService.findByUsername(username);
        return projectDAO.findAllByUser(user.getId());
    }


    public PageResult<Project> findByPage(ProjectListForm pageForm) {
        Project query = new Project();
        query.setProjectName(pageForm.getProjectName());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Project> projectList = projectDAO.findBy(query);
        PageInfo<Project> info = new PageInfo(projectList);
        PageResult<Project> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public long count() {
        return projectDAO.count();
    }
}
