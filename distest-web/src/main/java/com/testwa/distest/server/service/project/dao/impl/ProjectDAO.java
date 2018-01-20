package com.testwa.distest.server.service.project.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.mapper.ProjectMapper;
import com.testwa.distest.server.service.project.dao.IProjectDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class ProjectDAO extends BaseDAO<Project, Long> implements IProjectDAO {

    @Resource
    private ProjectMapper projectMapper;

    public List<Project> findBy(Project entity) {
        return projectMapper.findBy(entity);
    }

    @Override
    public List<Project> findAllByUser(Long userId) {
        return projectMapper.findAllByUser(userId, null);
    }

    @Override
    public List<Project> findAllByUser(Long userId, String projectName) {
        return projectMapper.findAllByUser(userId, projectName);
    }

    @Override
    public long count(Project query) {
        return projectMapper.countBy(query);
    }

    @Override
    public Project findOne(Long projectId) {
        return projectMapper.findOne(projectId);
    }
    @Override
    public Project fetchOne(Long projectId) {
        return projectMapper.fetchOne(projectId);
    }

    @Override
    public List<Project> findAllOrder(List<Long> projectIds, String order) {
        return projectMapper.findAllOrder(projectIds, order);
    }

}