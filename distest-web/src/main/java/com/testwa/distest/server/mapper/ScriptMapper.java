package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Script;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptMapper extends BaseMapper<Script, Long> {

	List<Script> findBy(@Param("entity") Script entity, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<Script> findByFromTestcase(Long TestcaseId);

    List<Script> findList(@Param("keys") List<Long> keys, @Param("projectId") Long projectId, @Param("orderBy") String orderBy);

    Script findOne(Long key);

    Script findOneInProject(@Param("key") Long entityId, @Param("projectId") Long projectId);

    Long countBy(Script entity);

    void disable(@Param("key") Long entityId);

    void disableAll(@Param("keys") List<Long> entityIds);
}