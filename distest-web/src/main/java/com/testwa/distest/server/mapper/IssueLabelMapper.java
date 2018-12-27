package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.annotation.VersionLocker;
import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueLabelMapper extends BaseMapper<IssueLabel, Long> {

    IssueLabel getByName(@Param("projectId") Long projectId, @Param("name") String name);

    void incr(@Param("labelId") Long labelId);

    void decr(@Param("labelId") Long labelId);

    void decrByProjectId(@Param("projectId") Long projectId);

    List<IssueLabel> listByIssueId(@Param("issueId") Long issueId);

}