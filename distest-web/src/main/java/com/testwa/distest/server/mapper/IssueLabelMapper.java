package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueLabelMapper extends BaseMapper<IssueLabel, Long> {

    IssueLabel findOne(@Param("labelId") Long labelId);

    IssueLabel getByName(@Param("projectId") Long projectId, @Param("name") String name);
}