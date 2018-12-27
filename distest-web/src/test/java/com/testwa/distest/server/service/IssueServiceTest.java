package com.testwa.distest.server.service;

import com.github.pagehelper.PageInfo;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.form.IssueNewForm;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.web.issue.vo.IssueVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class IssueServiceTest {
    private Issue entity;

    @Autowired
    private IssueService service;

    @Before
    public void before() {
        Long projectId = 18L;
        IssueNewForm form = new IssueNewForm();
        form.setTitle("test");
        form.setContent("test");
        List<String> labelName = new ArrayList<>();
        labelName.add("bug");
        labelName.add("enhancement");
        labelName.add("duplicate");
        form.setLabelName(labelName);

        this.entity = service.save(form, projectId);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testGet() {
        Issue issue1 = service.get(this.entity.getId());
        Assert.assertNotNull(issue1);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {

        Issue issue1 = service.get(this.entity.getId());
        Assert.assertNotNull(issue1);

        service.updateState(this.entity.getId(), DB.IssueStateEnum.CLOSED);
        Issue issue2 = service.get(this.entity.getId());
        Assert.assertEquals(issue2.getState(), DB.IssueStateEnum.CLOSED);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDelete() {
        Issue issue1 = service.get(this.entity.getId());
        Assert.assertNotNull(issue1);

        service.delete(this.entity.getId());
        Issue issue2 = service.get(this.entity.getId());
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
        form.setPageSize(5);
        form.setState("open");

        PageInfo<Issue> issues1 = service.page(form, projectId);


        for(int i=0; i<loopTime; i++) {
            IssueNewForm newForm = new IssueNewForm();
            newForm.setTitle("test" + i);
            newForm.setContent("test");
            List<String> labelName = new ArrayList<>();
            labelName.add("bug");
            labelName.add("enhancement");
            labelName.add("duplicate");
            form.setLabelName(String.join(",", labelName));
            service.save(newForm, projectId);
        }

        PageInfo<Issue> issues2 = service.page(form, projectId);

        Assert.assertEquals(issues2.getTotal(), issues1.getTotal() + loopTime);
        Assert.assertEquals(issues2.getPageSize(), form.getPageSize());
    }


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testStateCount() {

    }

}
