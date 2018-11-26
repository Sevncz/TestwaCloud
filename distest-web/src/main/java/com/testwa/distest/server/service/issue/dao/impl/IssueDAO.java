package com.testwa.distest.server.service.issue.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.mapper.IssueMapper;
import com.testwa.distest.server.service.issue.dao.IIssueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IssueDAO extends BaseDAO<Issue, Long> implements IIssueDAO {
    @Autowired
    private IssueMapper mapper;

    @Override
    public Issue findOne(Long issueId) {
        return mapper.findOne(issueId);
    }

    @Override
    public List<Issue> findBy(Issue query) {
        return mapper.findBy(query);
    }

    @Override
    public void updateState(Long issueId, DB.IssueStateEnum stateEnum) {
        Issue updateIssue = new Issue();
        updateIssue.setId(issueId);
        updateIssue.setState(stateEnum);
        super.update(updateIssue);
    }

    @Override
    public void delete(Long issueId) {
        Issue updateIssue = new Issue();
        updateIssue.setId(issueId);
        updateIssue.setEnabled(false);
        super.update(updateIssue);
    }
}
