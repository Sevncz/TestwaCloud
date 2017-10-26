package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.mvc.entity.App;
import com.testwa.distest.server.mvc.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AppMapper extends BaseMapper<App, Long> {

	List<App> findBy(App entity);

    List<App> findByFromProject(@Param("params") Map<String, Object> params);
}