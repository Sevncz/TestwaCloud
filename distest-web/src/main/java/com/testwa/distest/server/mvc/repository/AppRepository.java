package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.App;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface AppRepository extends CommonRepository<App, Serializable> {

    List<App> findByUserId(String userId);

    List<App> findByProjectId(String projectId);

    Integer countByProjectId(String projectId);
}
