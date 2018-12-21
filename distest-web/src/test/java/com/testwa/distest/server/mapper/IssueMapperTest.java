package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.IssueCondition;
import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.Issue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-20 10:47
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class IssueMapperTest {

    private Issue entity;

    @Autowired
    private IssueMapper mapper;

    @Before
    public void before() {
        Issue issue = new Issue();
        issue.setState(DB.IssueStateEnum.OPEN);
        issue.setProjectId(32L);
        issue.setAssigneeId(8L);
        issue.setTitle("title");

        issue.setAuthorId(7L);
        issue.setCreateTime(new Date());
        issue.setEnabled(true);

        mapper.insert(issue);

        this.entity = issue;
    }

    @Test
    public void testSelectById() {
        Issue entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<Issue> entityList = mapper.selectListByProperty(Issue::getAuthorId, this.entity.getAuthorId());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        IssueCondition condition = new IssueCondition();
        condition.setTitle("%title%");
        List<Issue> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        Long authorId = 1L;
        Issue entity1 = mapper.selectById(this.entity.getId());
        entity1.setAuthorId(authorId);
        mapper.update(entity1);
        Issue entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getAuthorId(), authorId);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        Issue entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        IssueCondition condition = new IssueCondition();
        condition.setAuthorId(this.entity.getAuthorId());
        Long count = mapper.count(condition);
        List<Issue> entityList = mapper.selectByCondition(condition);
        Assert.assertEquals(count, Long.valueOf(entityList.size()));
    }



}
