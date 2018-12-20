package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.Api;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiMapper extends BaseMapper<Api, Long> {


}