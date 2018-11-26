package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Issue;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueMapper extends BaseMapper<Issue, Long> {

    List<Issue> findBy(Issue query);

    Issue findOne(Long issueId);

}