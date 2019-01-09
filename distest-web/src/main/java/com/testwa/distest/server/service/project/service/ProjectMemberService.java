package com.testwa.distest.server.service.project.service;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.AuthorizedException;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.exception.DBException;
import com.testwa.distest.server.condition.ProjectMemberCondition;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ProjectMemberMapper;
import com.testwa.distest.server.service.project.form.MembersModifyForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class ProjectMemberService extends BaseService<ProjectMember, Long> {

    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProjectOwner(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.OWNER);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProjectMember(Long projectId, Long userId) {
        addMember(projectId, userId, DB.ProjectRole.MEMBER);
    }

    private void addMember(Long projectId, Long userId, DB.ProjectRole projectRole) {
        ProjectMember pm = new ProjectMember();
        pm.setMemberId(userId);
        pm.setProjectId(projectId);
        pm.setProjectRole(projectRole);
        pm.setCreateTime(new Date());
        projectMemberMapper.insert(pm);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long projectId, Long memberId) {
        projectMemberMapper.deleteMember(projectId, memberId);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addMembers(MembersModifyForm form) {
        if(form.getUsernames() == null || form.getUsernames().isEmpty()){
            return;
        }
        Project project = projectService.get(form.getProjectId());
        if (!project.getCreateBy().equals(currentUser.getId())) {
            log.error("login auth not owner of the project, projectId: {}, currentUsername: {}", form.getProjectId(), currentUser.getUsername());
            throw new AuthorizedException(ResultCode.ILLEGAL_OP, "您不是项目所有者，无法添加项目成员");
        }
        List<User> members = userService.findByUsernames(form.getUsernames());
        // 检查是否有用户不在系统
        if(!(members != null && members.size() == form.getUsernames().size())){
            log.error("members size is {}, usernames size is {}", members != null?members.size():0, form.getUsernames().size());
            throw new BusinessException(ResultCode.CONFLICT, "有成员不存在");
        }

        List<ProjectMember> pms = new ArrayList<>();
        members.forEach(m -> {
            ProjectMember p = new ProjectMember();
            p.setMemberId(m.getId());
            p.setInviteBy(currentUser.getId());
            p.setProjectId(project.getId());
            p.setCreateTime(new Date());
            p.setProjectRole(DB.ProjectRole.MEMBER);
            pms.add(p);
        });
        projectMemberMapper.mergeInsert(pms);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMemberList(Project project, Set<Long> memberIds) {
        memberIds.remove(project.getCreateBy());
        if(memberIds.isEmpty()) {
            return;
        }
        if(memberIds.size() == 1) {
            List<Long> memberList = new ArrayList<>(memberIds);
            int deleteNum = projectMemberMapper.deleteMember(project.getId(), memberList.get(0));
            log.info("Delete from members {}", deleteNum);
        }else{
            projectMemberMapper.deleteMemberList(project.getId(), memberIds);
        }
    }

    public List<User> listMembers(Long projectId){
        ProjectMemberCondition condition = new ProjectMemberCondition();
        condition.setProjectId(projectId);
        List<ProjectMember> members = projectMemberMapper.selectByCondition(condition);
        List<Long> memberIds = members.stream().map(ProjectMember::getMemberId).collect(Collectors.toList());
        return userService.findAll(memberIds);
    }

    public List<Map> listMembers(Long projectId, User userquery){
        Project project = projectService.get(projectId);
        return projectMemberMapper.findUsersProject(project.getId(), userquery);
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
            userquery.setMobile(phone);
        }
        List<Map> allUsers = listMembers(projectId, userquery);
        List<UserVO> in = new ArrayList<>();
        List<UserVO> out = new ArrayList<>();
        allUsers.forEach(u -> {
            UserVO vo = new UserVO();
            vo.setId(((BigInteger) u.get("id")).longValue());
            vo.setUsername((String) u.get("username"));
            vo.setHeader((String) u.get("header"));
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

    public ProjectMember getByProjectIdAndMemberId(Long projectId, Long memberId){
        return projectMemberMapper.getByProjectIdAndMemberId(projectId, memberId);
    }

    public List<ProjectMember> getByProjectIdAndMemberId(Long projectId, List<Long> memberId){
        return projectMemberMapper.listByProjectIdAndMembers(projectId, memberId);
    }

    public ProjectMember getProjectRole(Long projectId, Long userId) throws DBException {
        ProjectMember query = new ProjectMember();
        query.setMemberId(userId);
        query.setProjectId(projectId);
        List<ProjectMember> result = projectMemberMapper.findBy(query);
        if(result.isEmpty()){
            return null;
        }
        if(result.size() > 1){
            throw new DBException(ResultCode.SERVER_ERROR, "DB ERROR: ProjectMember double key");
        }
        return result.get(0);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int delAllMembers(Long projectId) {
        return projectMemberMapper.deleteMember(projectId, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRole(Long projectId, Long userId, Integer roleId) {
        DB.ProjectRole projectRole = DB.ProjectRole.valueOf(roleId);
        if(projectRole == null) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "角色信息错误");
        }
        ProjectMember projectMember = projectMemberMapper.getByProjectIdAndMemberId(projectId, userId);
        projectMemberMapper.updateProperty(ProjectMember::getProjectRole, projectRole, projectMember.getId());

    }
}
