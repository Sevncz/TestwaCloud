package com.testwa.distest.server.service.project.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.dao.IAppDAO;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.project.dao.IProjectDAO;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.script.dao.IScriptDAO;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.project.vo.ProjectStatis;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by wen on 16/9/1.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProjectService {

    @Autowired
    private IProjectDAO projectDAO;
    @Autowired
    private IAppDAO appDAO;
    @Autowired
    private IScriptDAO scriptDAO;
    @Autowired
    private ITestcaseDAO testcaseDAO;
    @Autowired
    private ITaskDAO taskDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private ViewMgr viewMgr;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private IDeviceDAO deviceDAO;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceService deviceService;

    /**
     * 保存project，同时保存projectMember for owner
     * @param entity
     * @return
     */
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
    public void delete(Long projectId) {
        projectDAO.disable(projectId);
    }

    /**
     * 返回删除的项目数量
     * @param projectIds
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> projectIds) {
        projectDAO.disableAll(projectIds);
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

    public List<Project> findByProjectOrder(List<Long> projectIds) {
        StringBuffer orderSb = new StringBuffer();
        orderSb.append("field(id,");
        String order = Joiner.on(",").join(projectIds);
        orderSb.append(order).append(")");
        List<Project> projectList = projectDAO.findAllOrder(projectIds, orderSb.toString());
        return projectList;
    }


    public long getProjectCountByOwner(Long userId) {
        Project query = new Project();
        query.setCreateBy(userId);
        return projectDAO.count(query);
    }

    public List<Long> getRecentViewProject(String username) throws Exception {
        return viewMgr.getRecentViewProject(username);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Project save(ProjectNewForm form, User createUser) {
        Project project = new Project();
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setCreateBy(createUser.getId());
        // 保存项目的同时保存owner
        Long projectId = this.insert(project);
        project.setId(projectId);
        // 保存成员
        MembersModifyForm membersModifyForm = new MembersModifyForm();
        membersModifyForm.setProjectId(projectId);
        membersModifyForm.setUsernames(form.getMembers());
        projectMemberService.addMembers(membersModifyForm);
        return project;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Project update(ProjectUpdateForm form) {

        User currentUser = userService.findByUsername(WebUtil.getCurrentUsername());
        Project project = projectDAO.findOne(form.getProjectId());
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setUpdateTime(new Date());
        project.setUpdateBy(currentUser.getId());
        projectDAO.update(project);

        List<User> members = projectMemberService.findAllMembers(project.getId());
        List<Long> needDelMember = new ArrayList<>();
        List<String> noNeedAddMember = new ArrayList<>();

        members.forEach(m -> {
            if(!form.getMembers().contains(m.getUsername())){
                needDelMember.add(m.getId());
            }else{
                noNeedAddMember.add(m.getUsername());
            }
        });
        if(!needDelMember.isEmpty()){
            projectMemberService.deleteMemberList(project, new HashSet<>(needDelMember));
        }

        MembersModifyForm membersModifyForm = new MembersModifyForm();
        membersModifyForm.setProjectId(form.getProjectId());
        List<String> updateMemebers = form.getMembers();
        updateMemebers.removeAll(noNeedAddMember);
        membersModifyForm.setUsernames(updateMemebers);
        if(!updateMemebers.isEmpty()){
            projectMemberService.addMembers(membersModifyForm);
        }
        return project;
    }

    public List<Project> findAllByUserList(String username) {
        User user = userService.findByUsername(username);
        if(user != null){
            return projectDAO.findAllByUser(user.getId());
        }
        return new ArrayList<>();
    }

    public PageResult<Project> findAllByUserPage(ProjectListForm pageForm, String username) {
        User user = userService.findByUsername(username);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.getPage().setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.getPage().setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Project> projectList = projectDAO.findAllByUser(user.getId(), pageForm.getProjectName());
        PageInfo<Project> info = new PageInfo(projectList);
        PageResult<Project> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }


    public PageResult<Project> findPage(ProjectListForm pageForm) {
        Project query = new Project();
        query.setProjectName(pageForm.getProjectName());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Project> projectList = projectDAO.findBy(query);
        PageInfo<Project> info = new PageInfo(projectList);
        PageResult<Project> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public long count() {
        return projectDAO.count();
    }

    public ProjectStatis statis(Long projectId) {
        App aq = new App();
        aq.setProjectId(projectId);
        aq.setEnabled(true);
        Long app = appDAO.countBy(aq);
        Script sq = new Script();
        sq.setProjectId(projectId);
        sq.setEnabled(true);
        Long script = scriptDAO.countBy(sq);
        Testcase tq = new Testcase();
        tq.setProjectId(projectId);
        tq.setEnabled(true);
        Long testcase = testcaseDAO.countBy(tq);
        Task kq = new Task();
        kq.setProjectId(projectId);
        kq.setEnabled(true);
        Long task = taskDAO.countBy(kq);

        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        int device = deviceIds.size();
        return new ProjectStatis(app, script, testcase, task, (long) device);
    }
}
