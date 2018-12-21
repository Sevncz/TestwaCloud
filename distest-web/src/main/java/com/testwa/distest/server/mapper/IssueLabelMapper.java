package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueLabelMapper extends BaseMapper<IssueLabel, Long> {

    IssueLabel getByName(@Param("projectId") Long projectId, @Param("name") String name);

    void addNum(Long labelId);

    List<IssueLabel> listByIssueId(@Param("issueId") Long issueId);
}