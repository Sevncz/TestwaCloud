package com.testwa.distest.server.service.issue.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.condition.BaseProjectCondition;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.IssueLabelDict;
import com.testwa.distest.server.entity.IssueLabelMap;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.IssueLabelDictMapper;
import com.testwa.distest.server.mapper.IssueLabelMapMapper;
import com.testwa.distest.server.mapper.IssueLabelMapper;
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
public class LabelService extends BaseService<IssueLabel, Long> {

    @Autowired
    private IssueLabelMapper labelMapper;
    @Autowired
    private IssueLabelMapMapper labelMapMapper;
    @Autowired
    private IssueLabelDictMapper issueLabelDictMapper;
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
        BaseProjectCondition condition = new BaseProjectCondition();
        condition.setProjectId(projectId);
        return labelMapper.selectByCondition(condition);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public IssueLabel save(IssueLabelNewForm form, Long projectId) {
        String name =form.getName();
        String color =form.getColor();
        return save(projectId, name, color);
    }

    protected IssueLabel save(Long projectId, String name, String color) {
        IssueLabel issueLabel = new IssueLabel();
        issueLabel.setProjectId(projectId);
        issueLabel.setName(name);
        issueLabel.setColor(color);
        issueLabel.setCreateBy(currentUser.getId());
        issueLabel.setEnabled(true);

        labelMapper.insert(issueLabel);
        return issueLabel;
    }

    /**
     * @Description: 删除标签，并且删除其引用
     * @Param: [labelId]
     * @Return: void
     * @Author wen
     * @Date 2018/12/21 11:10
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLabel(Long labelId) {
        delete(labelId);
        labelMapMapper.deleteByLabelId(labelId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(IssueLabelUpdateForm form) {
        IssueLabel issueLabel = get(form.getLabelId());
        issueLabel.setColor(form.getColor());
        issueLabel.setName(form.getName());

        labelMapper.update(issueLabel);

    }

    public IssueLabel getByName(Long projectId, String name) {
        return labelMapper.getByName(projectId, name);
    }

    /**
     * @Description: 根据 label 模板为项目创建默认label列表
     * @Param: [projectId]
     * @Return: void
     * @Author wen
     * @Date 2018/12/17 10:21
     */
    public void initForProject(final Long projectId) {
        List<IssueLabelDict> labelDicts = issueLabelDictMapper.list();
        labelDicts.forEach( defaultLabel -> {
            save(projectId, defaultLabel.getName(), defaultLabel.getColor());
        });
    }

    public List<IssueLabel> listByIssueId(Long issueId) {
        return labelMapper.listByIssueId(issueId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLabels(Long projectId, List<String> labelNames, Long issueId) {
        if(labelNames != null && !labelNames.isEmpty()) {
            // 删除旧的标签配置
            labelMapMapper.deleteByIssueId(issueId);
            labelMapper.decrByProjectId(projectId);

            // 添加新的标签配置
            labelNames.forEach( name -> {
                IssueLabel label = labelMapper.getByName(projectId, name);
                IssueLabelMap labelMap = new IssueLabelMap();
                labelMap.setIssueId(issueId);
                labelMap.setLabelId(label.getId());
                labelMap.setEnabled(true);
                labelMapMapper.insert(labelMap);
                // 引用数量 +1
                labelMapper.incr(label.getId());
            });
        }
    }
}

