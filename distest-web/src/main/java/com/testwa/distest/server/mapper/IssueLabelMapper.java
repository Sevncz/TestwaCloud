package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueLabelMapper extends BaseMapper<IssueLabel, Long> {

    IssueLabel getByName(@Param("projectId") Long projectId, @Param("name") String name);
}