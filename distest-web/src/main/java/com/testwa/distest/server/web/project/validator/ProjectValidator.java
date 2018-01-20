package com.testwa.distest.server.web.project.validator;

import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class ProjectValidator {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMemberService projectMemberService;


    public void validateProjectExist(List<Long> projectIds) throws ObjectNotExistsException {
        List<Project> projectList = projectService.findAll(projectIds);
        if(projectList == null || projectList.size() != projectIds.size()){
            throw new ObjectNotExistsException("项目不存在");
        }
    }

    public Project validateProjectExist(Long projectId) throws ObjectNotExistsException {
        Project project = projectService.findOne(projectId);
        if(project == null){
            throw new ObjectNotExistsException("项目不存在");
        }
        return project;
    }

    public void validateUserIsProjectMember(Long projectId, Long userId) throws AuthorizedException {
        ProjectMember member = projectMemberService.findByProjectIdAndMemberId(projectId, userId);
        if(member == null){
            throw new AuthorizedException("该用户不在项目中");
        }
    }

}
