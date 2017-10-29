package com.testwa.distest.server.service.project.service;

import com.testwa.distest.common.constant.ResultCode;
import com.testwa.core.common.enums.DB;
import com.testwa.distest.common.exception.*;
import com.testwa.core.entity.Project;
import com.testwa.core.entity.ProjectMember;
import com.testwa.core.entity.User;
import com.testwa.distest.server.service.project.dao.IProjectMemberDAO;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 20/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProjectMemberService {
    private static final Logger log = LoggerFactory.getLogger(ProjectMemberService.class);

    @Autowired
    private IProjectMemberDAO projectMemberDAO;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void saveProjectOwner(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.OWNER);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void saveProjectMember(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.MEMBER);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    private void addMember(Long projectId, Long userId, DB.ProjectRole projectRole) {
        ProjectMember pm = new ProjectMember();
        pm.setMemberId(userId);
        pm.setProjectId(projectId);
        pm.setProjectRole(projectRole);
        pm.setCreateTime(new Date());
        projectMemberDAO.insert(pm);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void delete(Long projectId, Long memberId) {
        projectMemberDAO.delete(projectId, memberId);

    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void addMembers(MembersModifyForm form) throws NoSuchProjectException, AccountAlreadyExistException, AccountException, AuthorizedException, ParamsException {

        Project project = projectService.findOne(form.getProjectId());

//        if (form.getUsernames().contains(getCurrentUsername())) {
//            log.error("can not add self, {}", form.toString());
//            throw new AccountAlreadyExistException("你已经在项目中");
//        }

        User owner = userService.findByUsername(getCurrentUsername());

        if (!project.getCreateBy().equals(owner.getId())) {
            log.error("login auth not owner of the project, projectId: {}, currentUsername: {}", form.getProjectId(), getCurrentUsername());
            throw new AuthorizedException("您不是项目所有者，无法添加项目成员");
        }

        // 检查是否有用户不在系统
        List<User> members = userService.findByUsernames(form.getUsernames());
        if(!(members != null && members.size() == form.getUsernames().size())){
            log.error("members size is {}, usernames size is {}", members != null?members.size():0, form.getUsernames().size());
            throw new ParamsException("有成员不存在");
        }

        List<ProjectMember> pms = new ArrayList<>();
        members.forEach(m -> {
            ProjectMember p = new ProjectMember();
            p.setMemberId(m.getId());
            p.setInviterId(owner.getId());
            p.setProjectId(project.getId());
            p.setCreateTime(new Date());
            p.setProjectRole(DB.ProjectRole.MEMBER);
            pms.add(p);
        });
        projectMemberDAO.mergeInsert(pms);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void delMembers(MembersModifyForm form) throws NoSuchProjectException {

        Project project = projectService.findOne(form.getProjectId());
        List<User> members = userService.findByUsernames(form.getUsernames());
        List<Long> memberIds = new ArrayList<>();
        members.forEach(m -> {
            memberIds.add(m.getId());
        });

        projectMemberDAO.deleteMembersFromProject(project.getId(), memberIds);
    }

    public List<User> findAllMembers(Long projectId) throws NoSuchProjectException {

        Project project = projectService.findOne(projectId);
        return projectMemberDAO.findMembersFromProject(project.getId());
    }

    public List<Map> findMembers(Long projectId, User userquery){

        Project project = projectService.findOne(projectId);
        return projectMemberDAO.findUsersProject(project.getId(), userquery);
    }


    public Map<String, List<UserVO>> findMembers(Long projectId, String memberName, String email, String phone) {
        User userquery = new User();
        userquery.setUsername(memberName);
        userquery.setEmail(email);
        userquery.setPhone(phone);
        List<Map> allUsers = findMembers(projectId, userquery);
        List<UserVO> in = new ArrayList<>();
        List<UserVO> out = new ArrayList<>();
        allUsers.forEach(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            if("in".equals(u.get("flag"))){
                in.add(vo);
            }
            if("out".equals(u.get("flag"))){
                out.add(vo);
            }
        });
        Map<String, List<UserVO>> result = new HashMap<>();
        result.put("inPro", in);
        result.put("out", out);
        return result;
    }



    public ProjectMember getProjectRole(Long projectId) throws AccountException, DBException {
        User user = userService.findByUsername(getCurrentUsername());
        ProjectMember query = new ProjectMember();
        query.setMemberId(user.getId());
        query.setProjectId(projectId);
        List<ProjectMember> result = projectMemberDAO.findBy(query);
        if(result.size() == 0){
            return null;
        }
        if(result.size() > 1){
            throw new DBException("DB ERROR: ProjectMember double key");
        }
        return result.get(0);
    }
}
