package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueAssignee;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueAssigneeMapper extends BaseMapper<IssueAssignee, Long> {

    int deleteByIssueId(@Param("issueId") Long issueId);

    int deleteByIssueIdAndAssigneeId(@Param("issueId") Long issueId, @Param("assigneeId") Long assigneeId);

    IssueAssignee getByIssueIdAndAssigneeId(@Param("issueId") Long issueId, @Param("assigneeId") Long assigneeId);
}