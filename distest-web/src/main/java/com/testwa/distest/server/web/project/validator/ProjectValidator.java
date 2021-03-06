package com.testwa.distest.server.web.project.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.AuthorizedException;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class ProjectValidator {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMemberService projectMemberService;


    public void validateProjectExist(List<Long> projectIds) {
        List<Project> projectList = projectService.findAll(projectIds);
        if(projectList == null || projectList.size() != projectIds.size()){
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
    }

    public Project validateProjectExist(Long projectId) {
        Project project = projectService.get(projectId);
        if(project == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        return project;
    }

    public void validateUserIsProjectMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberService.getByProjectIdAndMemberId(projectId, userId);
        if(member == null){
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该用户不在项目中");
        }
    }

    /**
     *@Description: 检查用户是否在任何项目里
     *@Param: [id]
     *@Return: void
     *@Author: wen
     *@Date: 2018/6/1
     */
    public void validateUserInAnyProject(Long userId) {
        List<Project> projectList = projectService.listByUser(userId);
        if(projectList.isEmpty()){
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该用户不在项目中");
        }
    }


    public void checkProjectAdmin(Long entityId, Long userId) {
        ProjectMember projectMember = projectMemberService.getByProjectIdAndMemberId(entityId, userId);
        if(projectMember != null) {
            if (DB.ProjectRole.MEMBER.equals(projectMember.getProjectRole())) {
                throw new AuthorizedException(ResultCode.ILLEGAL_OP, "您无法更改项目");
            }
        }
    }
}
