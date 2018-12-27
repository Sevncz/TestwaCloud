package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.service.issue.form.IssueLabelNewForm;
import com.testwa.distest.server.service.issue.form.IssueLabelUpdateForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.service.issue.service.LabelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class IssueLabelServiceTest {

    @Autowired
    private LabelService labelService;

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {
        // 保存一个标签
        IssueLabelNewForm form = new IssueLabelNewForm();
        form.setColor("000000");
        form.setName("test");
        IssueLabel issueLabel = labelService.save(form, 15L);

        IssueLabel label1 = labelService.get(issueLabel.getId());
        Assert.assertEquals(label1.getId().longValue(), issueLabel.getId().longValue());


        // 更新一个标签
        IssueLabelUpdateForm updateForm = new IssueLabelUpdateForm();
        updateForm.setLabelId(issueLabel.getId());
        updateForm.setName("test1");
        updateForm.setColor("111111");

        labelService.update(updateForm);

        // 获得刚才更新的标签
        IssueLabel label2 = labelService.get(issueLabel.getId());
        Assert.assertEquals(label2.getName(), updateForm.getName());
        Assert.assertEquals(label2.getColor(), updateForm.getColor());
        Assert.assertEquals(label2.getId(), updateForm.getLabelId());
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDelete() {
        // 保存一个标签
        IssueLabelNewForm form = new IssueLabelNewForm();
        form.setColor("000000");
        form.setName("test");
        IssueLabel label = labelService.save(form, 15L);

        IssueLabel label1 = labelService.get(label.getId());
        Assert.assertEquals(label1.getId().longValue(), label.getId().longValue());


        // 更新一个标签
        IssueLabelUpdateForm updateForm = new IssueLabelUpdateForm();
        updateForm.setLabelId(label.getId());
        updateForm.setName("test1");
        updateForm.setColor("111111");

        labelService.update(updateForm);

        labelService.delete(label.getId());
        IssueLabel label2 = labelService.get(label.getId());
        Assert.assertNull(label2);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testList() {
        // 保存一个标签
        int loopTime = 10;
        Long projectId = 1L;

        List<IssueLabel> issueLabels1 = labelService.list(projectId);

        for(int i=0; i<loopTime; i++) {
            IssueLabelNewForm form = new IssueLabelNewForm();
            form.setColor("000000");
            form.setName("test" + i);
            labelService.save(form, projectId);
        }

        List<IssueLabel> issueLabels2 = labelService.list(projectId);
        Assert.assertEquals(issueLabels2.size(), loopTime + issueLabels1.size());
    }



}
