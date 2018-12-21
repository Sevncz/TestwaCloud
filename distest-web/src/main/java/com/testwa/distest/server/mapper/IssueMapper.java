package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.entity.Issue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueMapper extends BaseMapper<Issue, Long> {

    List<Issue> listByCondition(@Param("query") IssueCondition query, @Param("labelIds") List<Long> labelIds);
}