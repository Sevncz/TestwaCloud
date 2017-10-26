package com.testwa.distest.server.service.script.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.App;
import com.testwa.distest.server.mvc.entity.Script;
import com.testwa.distest.server.mvc.mapper.AppMapper;
import com.testwa.distest.server.mvc.mapper.ScriptMapper;
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
    public List<Script> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}