package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.ScriptMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptCaseMapper extends BaseMapper<ScriptCase, Long> {

}