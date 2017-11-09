package com.testwa.distest.server.service.project.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.entity.Project;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface IProjectDAO extends IBaseDAO<Project, Long> {
    List<Project> findBy(Project entity);

    List<Project> findAllByUser(Long userId);

    Integer count(Project query);
}
