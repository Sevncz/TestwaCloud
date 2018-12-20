package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.condition.TestcaseCondition;
import com.testwa.distest.server.condition.TestcaseDetailCondition;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.TestcaseDetail;
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
public class TestcaseDetailMapperTest {

    private TestcaseDetail entity;

    @Autowired
    private TestcaseDetailMapper mapper;

    @Before
    public void before() {
        TestcaseDetail detail = new TestcaseDetail();
        detail.setTestcaseId(1L);
        detail.setScriptId(1L);
        detail.setSeq(1);

        detail.setEnabled(true);

        mapper.insert(detail);

        this.entity = detail;
    }

    @Test
    public void testSelectById() {
        TestcaseDetail entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<TestcaseDetail> entityList = mapper.selectListByProperty(TestcaseDetail::getScriptId, this.entity.getScriptId());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        TestcaseCondition condition = new TestcaseCondition();
        condition.setAppName("%APP%");
        List<TestcaseDetail> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        int seq = 10;
        TestcaseDetail entity1 = mapper.selectById(this.entity.getId());
        entity1.setSeq(seq);
        mapper.update(entity1);
        TestcaseDetail entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getSeq(), seq);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        TestcaseDetail entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        TestcaseDetailCondition condition = new TestcaseDetailCondition();
        condition.setTestcaseId(this.entity.getTestcaseId());
        Long count = mapper.count(condition);
        List<TestcaseDetail> entityList = mapper.selectByCondition(condition);
        Assert.assertEquals(count, Long.valueOf(entityList.size()));
    }



}
