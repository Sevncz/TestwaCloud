package com.testwa.distest.server.service.issue.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.IssueLabel;

import java.util.List;

public interface IIssueLabelDAO extends IBaseDAO<IssueLabel, Long> {

    List<IssueLabel> listByProjectId(Long projectId);

    IssueLabel findOne(Long labelId);

    void update(Long labelId, String name, String color);

    void delete(Long labelId);

    IssueLabel getByName(Long projectId, String name);
}
