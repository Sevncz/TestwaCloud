package com.testwa.distest.server.service.project.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IProjectDAO extends IBaseDAO<Project, Long> {
    List<Project> findBy(Project entity);

    List<Project> findAllByUser(Long userId);
    List<Project> findAllByUser(Long userId, String projectName);

    long count(Project query);

    Project findOne(Long projectId);

    List<Project> findAllOrder(List<Long> projectIds, String order);

    void disableAll(List<Long> projectIds);

    void disable(Long projectId);
}
