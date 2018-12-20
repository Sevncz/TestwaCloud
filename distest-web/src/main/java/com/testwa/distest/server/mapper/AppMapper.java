package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.App;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppMapper extends BaseMapper<App, Long> {

	List<App> findBy(App entity);

    List<App> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    App findOneInProject(@Param("key") Long entityId, @Param("projectId") Long projectId);

    Long countBy(App entity);

    void disableAll(List<Long> entityIds);

    void disableAllBy(@Param("packageName") String packageName, @Param("projectId") Long projectId);

    List<App> getAllVersion(@Param("packageName") String packageName, @Param("projectId") Long projectId);
}