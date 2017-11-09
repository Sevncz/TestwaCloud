package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.Script;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ScriptMapper extends BaseMapper<Script, Long> {

	List<Script> findBy(Script entity);

    List<Script> findByFromProject(@Param("params") Map<String, Object> params);

    List<Script> findByFromTestcase(Long TestcaseId);
}