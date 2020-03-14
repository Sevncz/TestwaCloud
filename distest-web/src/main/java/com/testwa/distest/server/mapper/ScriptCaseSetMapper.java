package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.ScriptCaseSet;
import org.springframework.stereotype.Repository;


@Repository
public interface ScriptCaseSetMapper extends BaseMapper<ScriptCaseSet, Long> {
}