package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.ScriptMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptMetadataMapper extends BaseMapper<ScriptMetadata, Long> {

}