package com.testwa.distest.server.mvc.service;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.exception.NoSuchProjectException;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.repository.*;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.mvc.service.cache.WebCacheService;
import com.testwa.distest.server.mvc.vo.ProjectStats;
import com.testwa.distest.server.mvc.vo.ProjectVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class ProjectService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TestcaseRepository testcaseRepository;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private ScriptRepository scriptRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private WebCacheService webCacheService;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private AppService appService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void deleteById(String projectId) {
        projectRepository.delete(projectId);
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(projectId));

        Update update = new Update();
        update.set("modifyDate", new Date());
        projectRepository.updateMulti(query, update);

        deleteAllRelatedObjectByProjectId(projectId);
    }

    public void deleteAllRelatedObjectByProjectId(String projectId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("projectId").is(projectId));

        Update update = new Update();
        update.set("disable", true);
        update.set("modifyDate", new Date());

        taskRepository.updateMulti(query, update);
        appRepository.updateMulti(query, update);
        scriptRepository.updateMulti(query, update);
        testcaseRepository.updateMulti(query, update);
        reportRepository.updateMulti(query, update);
    }

    public Project getProjectById(String projectId) {
        return projectRepository.findOne(projectId);
    }

    public Page<Project> findAll(PageRequest pageRequest) {
        return projectRepository.findAll(pageRequest);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public void saveProjectOwner(String projectId, String userId) {
        addMember(projectId, userId, true, 0);
    }

    public void saveProjectMember(String projectId, String userId) {
        addMember(projectId, userId, false, -1);
    }

    private void addMember(String projectId, String userId, Boolean isOwner, Integer role) {
        ProjectMember pm = new ProjectMember();
        pm.setJoinTime(TimeUtil.getTimestampLong());
        pm.setMemberId(userId);
        pm.setProjectId(projectId);
        pm.setRole(role);
        pm.setOwner(isOwner);
        projectMemberRepository.save(pm);
    }

    public void delMember(String projectId, String memberId) {

        ProjectMember pm = projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId);
        if (pm != null) {

            projectMemberRepository.delete(pm);

        }

    }

    /**
     * 获得用户创建的和用户参加的所有project
     *
     * @param user
     * @return
     */
    public List<Project> findByUser(User user) {
        List<Project> ownerProjects = new ArrayList<>();
        List<ProjectMember> joinProjects = projectMemberRepository.findByMemberId(user.getId());
        if (joinProjects == null) {
            joinProjects = new ArrayList<>();
        } else {
            Query query = new Query();
            query.addCriteria(Criteria.where("id").in(joinProjects.stream().map(s -> s.getProjectId()).collect(Collectors.toList())));
            List<Project> project = projectRepository.find(query);
            ownerProjects.addAll(project);
        }
        return ownerProjects;
    }

    public List<Project> findByUser(String username) {
        User user = userRepository.findByUsername(username);
        return findByUser(user);
    }

    public Project findById(String projectId) {
        return projectRepository.findById(projectId);
    }

    public List<ProjectMember> getMembersByProject(String projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    public List<User> getUserMembersByProject(String projectId) {
        List<User> users = new ArrayList<>();
        List<ProjectMember> projectMembers = getMembersByProject(projectId);
        if (projectMembers != null && projectMembers.size() > 0) {
            List<String> userIds = new ArrayList<>();
            for (ProjectMember pm : projectMembers) {
                userIds.add(pm.getMemberId());
            }
            users = userService.findByUserIds(userIds);
        }
        return users;
    }

    public Page<Project> findPage(PageRequest pageRequest, List<String> projectIds, String projectName) {

        List<Criteria> andCriteria = new ArrayList<>();
        if (StringUtils.isNotEmpty(projectName)) {
            andCriteria.add(Criteria.where("projectName").regex(projectName));
        }
        andCriteria.add(Criteria.where("id").in(projectIds));

        Query query = buildQueryByCriteria(andCriteria, null);
        return projectRepository.find(query, pageRequest);

    }

    public List<Project> findAll(List<String> projectIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(projectIds));
        return projectRepository.find(query);
    }

    public List<ProjectMember> getMembersByProjectAndUserId(String projectId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("projectId").is(projectId));
        query.addCriteria(Criteria.where("memberId").is(userId));

        return projectMemberRepository.find(query);
    }

    public Map<String, List<User>> getMembers(String projectId, String memberName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").regex(memberName));
        List<User> users = userRepository.find(query);

        Query query1 = new Query();
        query1.addCriteria(Criteria.where("memberId").in(users.stream().map(s -> s.getId()).collect(Collectors.toList())));
        query1.addCriteria(Criteria.where("projectId").is(projectId));
        List<ProjectMember> pms = projectMemberRepository.find(query1);

        List<User> projectUser = new ArrayList<>();
        for (User u : users) {
            for (ProjectMember p : pms) {
                if (u.getId().equals(p.getMemberId())) {
                    projectUser.add(u);
                }
            }
        }
        users.removeAll(projectUser);
        Map<String, List<User>> result = new HashMap<>();
        result.put("inPro", projectUser);
        result.put("out", users);
        return result;
    }

    public void delAllMember(String projectId) {
        List<ProjectMember> pms = projectMemberRepository.findByProjectId(projectId);
        projectMemberRepository.delete(pms);
    }

    private Project checkProject(String projectId) throws NoSuchProjectException {
        Project project = this.projectRepository.findOne(projectId);
        if (project == null) {
            throw new NoSuchProjectException("没有此项目!");
        }
        return project;
    }

    public ProjectMember getRoleNameByPro(String projectId, String userId) throws NoSuchProjectException {
        this.checkProject(projectId);
        Query query = new Query();
        query.addCriteria(Criteria.where("projectId").is(projectId));
        query.addCriteria(Criteria.where("memberId").is(userId));
        return projectMemberRepository.findOne(query);
    }

    public String enterProject(User user, String projectId) {
        // record in redis for user project history
        webCacheService.userProjectHistory(user, projectId);
        // return user role in project
        Project project = projectRepository.findOne(projectId);
        return project.getUserId() == user.getId() ? "admin" : "user";
    }

    public Integer getProjectCountByUser(User user) {
        return projectRepository.countByUserId(user.getId());
    }

    public List<Project> getHistoryUserProjectVO(User user) {
        List<String> projectIds = webCacheService.getUserProjectHistory(user.getId());
        if (null == projectIds) {
            return new ArrayList<>();
        }
        return projectRepository.findByIdIn(projectIds);
    }

    public ProjectStats getProjectStats(String projectId, User user) {
        // get available device count
        Integer devices = remoteClientService.getDeviceByUserIdAndProjectId(user.getId(), projectId).size();
        // apps
        Integer apps = appService.getCountAppByProjectId(projectId);
        // scripts
        Integer scripts = scriptService.getCountScriptByProjectId(projectId);
        // cases
        Integer cases = testcaseService.getCountCaseByProjectId(projectId);
        // tasks
        Integer tasks = taskService.getCountTaskByProjectId(projectId);
        // todo: reports
//        Integer reports =
        return new ProjectStats(devices, apps, scripts, cases, tasks, 0);
    }
}
