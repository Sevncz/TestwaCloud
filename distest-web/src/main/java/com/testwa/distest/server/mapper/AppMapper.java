package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.App;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppMapper extends BaseMapper<App, Long> {

	List<App> findBy(App entity);

    App findOne(Long key);

    List<App> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    List<App> findByFromProject(@Param("params") Map<String, Object> params);

    App findOneInProject(@Param("key") Long entityId, @Param("projectId") Long projectId);

    Long countBy(App entity);
}