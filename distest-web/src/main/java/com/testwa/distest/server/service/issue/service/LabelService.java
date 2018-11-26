package com.testwa.distest.server.service.issue.service;

import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.issue.dao.IIssueLabelDAO;
import com.testwa.distest.server.service.issue.form.IssueLabelNewForm;
import com.testwa.distest.server.service.issue.form.IssueLabelUpdateForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Program: distest
 * @Description:
 * @Author: wen
 * @Create: 2018-11-21 11:47
 **/
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class LabelService {

    @Autowired
    private IIssueLabelDAO issueLabelDAO;
    @Autowired
    private User currentUser;

    /**
     * @Description: 获得项目下的标签列表
     * @Param: [projectId]
     * @Return: java.util.List<com.testwa.distest.server.entity.IssueLabel>
     * @Author wen
     * @Date 2018/11/22 18:46
     */
    public List<IssueLabel> list(Long projectId) {
        return issueLabelDAO.listByProjectId(projectId);
    }

    /**
     * @Description: 获得一个标签
     * @Param: [labelId]
     * @Return: com.testwa.distest.server.entity.IssueLabel
     * @Author wen
     * @Date 2018/11/23 15:15
     */
    public IssueLabel findOne(Long labelId) {
        return issueLabelDAO.findOne(labelId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public long save(IssueLabelNewForm form, Long projectId) {
        IssueLabel issueLabel = new IssueLabel();
        issueLabel.setProjectId(projectId);
        issueLabel.setName(form.getName());
        issueLabel.setColor(form.getColor());
        issueLabel.setCreateBy(currentUser.getId());
        issueLabel.setEnabled(true);

        return issueLabelDAO.insert(issueLabel);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void update(IssueLabelUpdateForm form) {
        issueLabelDAO.update(form.getLabelId(), form.getName(), form.getColor());

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long labelId) {
        issueLabelDAO.delete(labelId);

    }

    public IssueLabel getByName(Long projectId, String name) {
        return issueLabelDAO.getByName(projectId, name);
    }
}
