package com.testwa.distest.server.web.project.validator;

import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.server.mvc.entity.Project;
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


    public void validateProjectExist(List<Long> projectIds) throws NoSuchProjectException {
        List<Project> projectList = projectService.findAll(projectIds);
        if(projectList == null || projectList.size() != projectIds.size()){
            throw new NoSuchProjectException("项目不存在");
        }
    }

    public Project validateProjectExist(Long projectId) throws NoSuchProjectException {
        Project project = projectService.findOne(projectId);
        if(project == null){
            throw new NoSuchProjectException("项目不存在");
        }
        return project;
    }

}
