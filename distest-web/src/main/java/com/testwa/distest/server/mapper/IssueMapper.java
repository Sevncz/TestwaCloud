package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.Issue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueMapper extends BaseMapper<Issue, Long> {

    List<Issue> findBy(Issue query);

    List<Issue> search(@Param("query") Issue query, @Param("search") String issueSearch);
}