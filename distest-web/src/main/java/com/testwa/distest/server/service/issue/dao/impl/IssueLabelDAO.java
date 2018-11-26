package com.testwa.distest.server.service.issue.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.mapper.IssueLabelMapper;
import com.testwa.distest.server.service.issue.dao.IIssueLabelDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IssueLabelDAO extends BaseDAO<IssueLabel, Long> implements IIssueLabelDAO {

    @Autowired
    private IssueLabelMapper mapper;

    @Override
    public List<IssueLabel> listByProjectId(Long projectId) {
        IssueLabel query = new IssueLabel();
        query.setProjectId(projectId);
        query.setEnabled(true);
        return super.findBy(query);
    }

    @Override
    public IssueLabel findOne(Long labelId) {
        return mapper.findOne(labelId);
    }

    @Override
    public void update(Long labelId, String name, String color) {
        IssueLabel updateLable = new IssueLabel();
        updateLable.setId(labelId);
        updateLable.setName(name);
        updateLable.setColor(color);
        super.update(updateLable);
    }

    @Override
    public void delete(Long labelId) {
        IssueLabel updateLable = new IssueLabel();
        updateLable.setId(labelId);
        updateLable.setEnabled(false);
        super.update(updateLable);
    }

    @Override
    public IssueLabel getByName(Long projectId, String name) {
        return mapper.getByName(projectId, name);
    }
}
