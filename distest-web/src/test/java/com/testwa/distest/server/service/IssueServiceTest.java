package com.testwa.distest.server.service;

import com.github.pagehelper.PageInfo;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.service.IssueService;
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

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class IssueServiceTest {

    @Autowired
    private IssueService issueService;

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {
        Long projectId = 23L;

        IssueNewForm form = new IssueNewForm();
        form.setTitle("test");
        form.setContent("test");
        form.setLabelName("bug");
        long issueId = issueService.save(form, projectId);

        Issue issue1 = issueService.get(issueId);
        Assert.assertEquals(issue1.getId().longValue(), issueId);

        issueService.updateState(issueId, DB.IssueStateEnum.CLOSED);
        Issue issue2 = issueService.get(issueId);
        Assert.assertEquals(issue2.getState(), DB.IssueStateEnum.CLOSED);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDelete() {
        Long projectId = 1L;

        IssueNewForm form = new IssueNewForm();
        form.setTitle("test");
        form.setContent("test");
        form.setLabelName("bug");
        long issueId = issueService.save(form, projectId);

        Issue issue1 = issueService.get(issueId);
        Assert.assertEquals(issue1.getId().longValue(), issueId);

        issueService.delete(issueId);
        Issue issue2 = issueService.get(issueId);
        Assert.assertNull(issue2);
    }


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testPage() {
        // 保存一个标签
        int loopTime = 20;
        Long projectId = 1L;

        IssueListForm form = new IssueListForm();
        form.setPageNo(1);
        form.setPageSize(20);
        form.setState("open");

        PageInfo<Issue> issues1 = issueService.page(form, projectId);

        for(int i=0; i<loopTime; i++) {
            IssueNewForm newForm = new IssueNewForm();
            newForm.setTitle("test" + i);
            newForm.setContent("test");
            newForm.setLabelName("bug");
            issueService.save(newForm, projectId);
        }

        PageInfo<Issue> issues2 = issueService.page(form, projectId);

        Assert.assertEquals(issues2.getTotal(), issues1.getTotal() + loopTime);
        Assert.assertEquals(issues2.getPages(), form.getPageSize());
    }
}
