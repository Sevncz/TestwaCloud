package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Script;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ScriptMapper extends BaseMapper<Script, Long> {

	List<Script> findBy(Script entity);

    List<Script> findByFromProject(@Param("params") Map<String, Object> params);

    List<Script> findByFromTestcase(Long TestcaseId);

    List<Script> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    Script findOne(Long key);
}