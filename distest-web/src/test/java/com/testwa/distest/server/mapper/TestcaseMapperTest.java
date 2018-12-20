package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.ScriptCondition;
import com.testwa.distest.server.condition.TestcaseCondition;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Testcase;
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
public class TestcaseMapperTest {

    private Testcase entity;

    @Autowired
    private TestcaseMapper mapper;

    @Before
    public void before() {
        Testcase testcase = new Testcase();
        testcase.setAppName("xxxAPP");
        testcase.setAppInfoId(1L);
        testcase.setCaseName("caseName");
        testcase.setDescription("desc");
        testcase.setPackageName("packageName");

        testcase.setCreateBy(7L);
        testcase.setCreateTime(new Date());
        testcase.setEnabled(true);

        mapper.insert(testcase);

        this.entity = testcase;
    }

    @Test
    public void testSelectById() {
        Testcase entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<Testcase> entityList = mapper.selectListByProperty(Testcase::getCreateBy, this.entity.getCreateBy());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        TestcaseCondition condition = new TestcaseCondition();
        condition.setAppName("%APP%");
        List<Testcase> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        String des = "xxxxx1";
        Testcase entity1 = mapper.selectById(this.entity.getId());
        entity1.setDescription(des);
        mapper.update(entity1);
        Testcase entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getDescription(), des);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        Testcase entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        TestcaseCondition condition = new TestcaseCondition();
        condition.setCreateBy(this.entity.getCreateBy());
        Long count = mapper.count(condition);
        List<Testcase> entityList = mapper.selectByCondition(condition);
        Assert.assertEquals(count, Long.valueOf(entityList.size()));
    }



}
