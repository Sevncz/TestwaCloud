package com.testwa.distest.server.service.issue.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;

import java.util.List;

public interface IIssueDAO extends IBaseDAO<Issue, Long> {
    Issue findOne(Long issueId);

    List<Issue> findBy(Issue query);

    void updateState(Long issueId, DB.IssueStateEnum stateEnum);

    void delete(Long issueId);
}
