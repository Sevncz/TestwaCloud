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
@TestPropertySource(locations="classpath:application-dev.properties")
public class IssueLabelServiceTest {

    @Autowired
    private LabelService labelService;

    @Test
    @Transactional
    @Rollback(true)
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {
        // 保存一个标签
        IssueLabelNewForm form = new IssueLabelNewForm();
        form.setColor("000000");
        form.setName("test");
        long labelId = labelService.save(form, 15L);

        IssueLabel label1 = labelService.findOne(labelId);
        Assert.assertEquals(label1.getId().longValue(), labelId);


        // 更新一个标签
        IssueLabelUpdateForm updateForm = new IssueLabelUpdateForm();
        updateForm.setLabelId(labelId);
        updateForm.setName("test1");
        updateForm.setColor("111111");

        labelService.update(updateForm);

        // 获得刚才更新的标签
        IssueLabel label2 = labelService.findOne(labelId);
        Assert.assertEquals(label2.getName(), updateForm.getName());
        Assert.assertEquals(label2.getColor(), updateForm.getColor());
        Assert.assertEquals(label2.getId(), updateForm.getLabelId());
    }

    @Test
    @Transactional
    @Rollback(true)
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDelete() {
        // 保存一个标签
        IssueLabelNewForm form = new IssueLabelNewForm();
        form.setColor("000000");
        form.setName("test");
        long labelId = labelService.save(form, 15L);

        IssueLabel label1 = labelService.findOne(labelId);
        Assert.assertEquals(label1.getId().longValue(), labelId);


        // 更新一个标签
        IssueLabelUpdateForm updateForm = new IssueLabelUpdateForm();
        updateForm.setLabelId(labelId);
        updateForm.setName("test1");
        updateForm.setColor("111111");

        labelService.update(updateForm);

        labelService.delete(labelId);
        IssueLabel label2 = labelService.findOne(labelId);
        Assert.assertNull(label2);
    }

    @Test
    @Transactional
    @Rollback(true)
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
