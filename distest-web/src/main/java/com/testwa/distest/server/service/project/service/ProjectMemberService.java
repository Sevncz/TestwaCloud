package com.testwa.distest.server.service.project.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.core.base.exception.*;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.dao.IProjectMemberDAO;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProjectMemberService {

    @Autowired
    private IProjectMemberDAO projectMemberDAO;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void saveProjectOwner(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.OWNER);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void saveProjectMember(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.MEMBER);
    }

    private void addMember(Long projectId, Long userId, DB.ProjectRole projectRole) {
        ProjectMember pm = new ProjectMember();
        pm.setMemberId(userId);
        pm.setProjectId(projectId);
        pm.setProjectRole(projectRole);
        pm.setCreateTime(new Date());
        projectMemberDAO.insert(pm);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long projectId, Long memberId) {
        projectMemberDAO.delete(projectId, memberId);

    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void addMembers(MembersModifyForm form) throws AuthorizedException, ParamsException {
        if(form.getUsernames() == null || form.getUsernames().size() == 0){
            return;
        }
        Project project = projectService.findOne(form.getProjectId());
        User owner = userService.findByUsername(getCurrentUsername());
        if (!project.getCreateBy().equals(owner.getId())) {
            log.error("login auth not owner of the project, projectId: {}, currentUsername: {}", form.getProjectId(), getCurrentUsername());
            throw new AuthorizedException("您不是项目所有者，无法添加项目成员");
        }
        List<User> members = userService.findByUsernames(form.getUsernames());
        // 检查是否有用户不在系统
        if(!(members != null && members.size() == form.getUsernames().size())){
            log.error("members size is {}, usernames size is {}", members != null?members.size():0, form.getUsernames().size());
            throw new ParamsException("有成员不存在");
        }

        List<ProjectMember> pms = new ArrayList<>();
        members.forEach(m -> {
            ProjectMember p = new ProjectMember();
            p.setMemberId(m.getId());
            p.setInviteBy(owner.getId());
            p.setProjectId(project.getId());
            p.setCreateTime(new Date());
            p.setProjectRole(DB.ProjectRole.MEMBER);
            pms.add(p);
        });
        projectMemberDAO.mergeInsert(pms);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delMembers(MembersModifyForm form) {

        Project project = projectService.findOne(form.getProjectId());
        List<User> members = userService.findByUsernames(form.getUsernames());
        if(members != null){
            List<Long> memberIds = new ArrayList<>();
            members.forEach(m -> {
                memberIds.add(m.getId());
            });

            projectMemberDAO.deleteMembersFromProject(project.getId(), memberIds);
        }
    }

    public List<User> findAllMembers(Long projectId){

        Project project = projectService.findOne(projectId);
        return projectMemberDAO.findMembersFromProject(project.getId());
    }

    public List<Map> findMembers(Long projectId, User userquery){

        Project project = projectService.findOne(projectId);
        return projectMemberDAO.findUsersProject(project.getId(), userquery);
    }


    public Map<String, List<UserVO>> queryMembersAndFlagIsInProject(Long projectId, String memberName, String email, String phone) {
        User userquery = new User();
        if(StringUtils.isNotEmpty(memberName)){
            userquery.setUsername(memberName);
        }
        if(StringUtils.isNotEmpty(email)){
            userquery.setEmail(email);
        }
        if(StringUtils.isNotEmpty(phone)){
            userquery.setPhone(phone);
        }
        List<Map> allUsers = findMembers(projectId, userquery);
        List<UserVO> in = new ArrayList<>();
        List<UserVO> out = new ArrayList<>();
        allUsers.forEach(u -> {
            UserVO vo = new UserVO();
            vo.setId(((BigInteger) u.get("id")).longValue());
            vo.setUsername((String) u.get("username"));
            vo.setEmail((String) u.get("email"));
            vo.setPhone((String) u.get("phone"));
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

    public ProjectMember findByProjectIdAndMemberId(Long projectId, Long memberId){
        return projectMemberDAO.findByProjectIdAndMember(projectId, memberId);
    }

    public List<ProjectMember> findByProjectIdAndMemberId(Long projectId, List<Long> memberId){
        return projectMemberDAO.findByProjectIdAndMembers(projectId, memberId);
    }



    public ProjectMember getProjectRole(Long projectId, Long userId) throws AccountException, DBException {
        ProjectMember query = new ProjectMember();
        query.setMemberId(userId);
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

    public int delAllMembers(Long projectId) {
        return projectMemberDAO.delete(projectId, null);
    }
}
