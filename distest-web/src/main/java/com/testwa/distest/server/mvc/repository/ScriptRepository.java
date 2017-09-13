package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Script;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface ScriptRepository extends CommonRepository<Script, Serializable> {
    List<Script> findByIdNotIn(List<String> scriptIds);

    List<Script> findByIdIn(List<String> scripts);

    List<Script> findByProjectId(String projectId);

    Integer countByProjectId(String projectId);
}
