package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppInfoMapper extends BaseMapper<AppInfo, Long> {

	List<AppInfo> findBy(AppInfo entity);

	List<AppInfo> findPage(@Param("query") AppInfo entity, @Param("orderBy") String orderBy,
                           @Param("desc") String desc, @Param("offset") int offset, @Param("limit") int limit);

    AppInfo findOne(Long key);

    List<AppInfo> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    AppInfo findOneInProject(@Param("key") Long entityId, @Param("projectId") Long projectId);

    Long countBy(AppInfo entity);

    void disable(Long entityId);

}