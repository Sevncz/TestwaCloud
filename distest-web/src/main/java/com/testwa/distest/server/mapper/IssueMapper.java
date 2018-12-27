package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IssueMapper extends BaseMapper<Issue, Long> {

    List<Issue> listByCondition(@Param("query") IssueCondition query, @Param("labelIds") List<Long> labelIds);

    List<IssueStateCountDTO> getCountGroupByState(@Param("query") IssueCondition query, @Param("labelIds") List<Long> labelIds);

    int incrCommentNum(@Param("issueId") Long issueId);

    int decrCommentNum(@Param("issueId") Long issueId);
}