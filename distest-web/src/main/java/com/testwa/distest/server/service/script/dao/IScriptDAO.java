package com.testwa.distest.server.service.script.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.entity.Script;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IScriptDAO extends IBaseDAO<Script, Long> {
    List<Script> findBy(Script entity);

    Script findOne(Long key);

    List<Script> findAll(List<Long> keys);

    List<Script> findByFromProject(Map<String, Object> params);
}
