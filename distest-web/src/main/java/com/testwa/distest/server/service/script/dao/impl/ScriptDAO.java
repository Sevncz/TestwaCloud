package com.testwa.distest.server.service.script.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.mapper.ScriptMapper;
import com.testwa.distest.server.service.script.dao.IScriptDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class ScriptDAO extends BaseDAO<Script, Long> implements IScriptDAO {

    @Resource
    private ScriptMapper mapper;

    public List<Script> findBy(Script app) {
        return mapper.findBy(app);
    }

    @Override
    public Script findOne(Long key) {
        return mapper.findOne(key);
    }

    @Override
    public List<Script> findAll(List<Long> keys) {
        return mapper.findList(keys, null, null);
    }

    @Override
    public List<Script> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

    @Override
    public List<Script> findAllInProject(List<Long> keys, Long projectId) {
        return mapper.findList(keys, projectId, null);
    }

    @Override
    public Script findOneInPorject(Long scriptId, Long projectId) {
        return mapper.findOneInProject(scriptId, projectId);
    }

    @Override
    public Long countBy(Script query) {
        return mapper.countBy(query);
    }

}