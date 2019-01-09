package com.testwa.distest.server.service.project.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.testwa.core.base.service.BaseService;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.condition.BaseProjectCondition;
import com.testwa.distest.server.condition.ProjectCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.*;
import com.testwa.distest.server.service.issue.service.LabelService;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.project.form.ProjectNewForm;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
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
public class ProjectService extends BaseService<Project, Long> {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private AppMapper appMapper;
    @Autowired
    private ScriptMapper scriptMapper;
    @Autowired
    private TestcaseMapper testcaseMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ViewMgr viewMgr;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private User currentUser;
    @Autowired
    private LabelService labelService;

    /**
     * 保存project，同时保存projectMember for owner
     * @param entity
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public long insert(Project entity) {
        entity.setCreateTime(new Date());
        projectMapper.insert(entity);
        projectMemberService.saveProjectOwner(entity.getId(), entity.getCreateBy());
        return entity.getId();
    }

    /**
     * 返回删除的项目数量
     * @param projectIds
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<Long> projectIds) {
        projectMapper.disableAll(projectIds);
    }

    public List<Project> findAll(List<Long> projectIds) {
        List<Project> projects = new ArrayList<>();
        projectIds.forEach( projectId -> {
            Project project = get(projectId);
            projects.add(project);
        });
        return projects;
    }

    public List<Project> findByProjectOrder(List<Long> projectIds) {
        StringBuilder orderSb = new StringBuilder();
        orderSb.append("field(id,");
        String order = Joiner.on(",").join(projectIds);
        orderSb.append(order).append(")");
        return projectMapper.findAllOrder(projectIds, orderSb.toString());
    }


    public long getProjectCountByOwner(Long userId) {
        ProjectCondition query = new ProjectCondition();
        query.setCreateBy(userId);
        return projectMapper.count(query);
    }

    public List<Long> getRecentViewProject() throws Exception {
        return viewMgr.getRecentViewProject();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Project save(ProjectNewForm form, User createUser) {
        Project project = new Project();
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setCreateBy(createUser.getId());
        // 保存项目的同时保存owner
        Long projectId = this.insert(project);
        project.setId(projectId);
        // 生成issue列表

        // 保存成员
        MembersModifyForm membersModifyForm = new MembersModifyForm();
        membersModifyForm.setProjectId(projectId);
        membersModifyForm.setUsernames(form.getMembers());
        projectMemberService.addMembers(membersModifyForm);

        // 初始化项目所使用的 issueLabel
        labelService.initForProject(projectId);

        return project;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Project update(ProjectUpdateForm form) {
        Project project = get(form.getProjectId());
        project.setProjectName(form.getProjectName());
        project.setDescription(form.getDescription());
        project.setUpdateTime(new Date());
        project.setUpdateBy(currentUser.getId());
        projectMapper.update(project);

        List<User> members = projectMemberService.listMembers(project.getId());
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

    public List<Project> listByUser(Long userId) {
        if(userId != null){
            return projectMapper.findAllByUser(userId, null);
        }
        return new ArrayList<>();
    }

    public PageResult<Project> pageByUser(ProjectListForm pageForm, User user) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        if(StringUtils.isBlank(pageForm.getOrderBy()) ){
            pageForm.setOrderBy("id");
        }
        if(StringUtils.isBlank(pageForm.getOrder()) ){
            pageForm.setOrder("desc");
        }
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Project> projectList = projectMapper.findAllByUser(user.getId(), pageForm.getProjectName());
        PageInfo<Project> info = new PageInfo(projectList);
        return new PageResult<>(info.getList(), info.getTotal());
    }


    public PageResult<Project> page(ProjectListForm pageForm) {
        Project query = new Project();
        query.setProjectName(pageForm.getProjectName());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Project> projectList = projectMapper.findBy(query);
        PageInfo<Project> info = new PageInfo(projectList);
        PageResult<Project> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public long count() {
        return projectMapper.count(null);
    }

    public ProjectStatis statis(Long projectId) {
        BaseProjectCondition condition = new BaseProjectCondition();
        condition.setProjectId(projectId);
        Long app = appMapper.count(condition);
        Long script = scriptMapper.count(condition);
        Long testcase = testcaseMapper.count(condition);
        Long task = taskMapper.count(condition);

        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        int device = deviceIds.size();
        return new ProjectStatis(app, script, testcase, task, (long) device);
    }
}
