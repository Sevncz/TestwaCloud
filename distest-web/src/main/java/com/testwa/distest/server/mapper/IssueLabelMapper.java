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

    @VersionLocker(value = true)
    void incr(@Param("labelId") Long labelId, @Param("lock_version") Long lockVersion);

    @VersionLocker(value = true)
    void decr(@Param("labelId") Long labelId, @Param("lock_version") Long lockVersion);

    void decrByProjectId(@Param("projectId") Long projectId);

    List<IssueLabel> listByIssueId(@Param("issueId") Long issueId);

}