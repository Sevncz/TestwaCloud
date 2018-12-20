package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.ScriptCondition;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Script;
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
public class ScriptMapperTest {

    private Script entity;

    @Autowired
    private ScriptMapper mapper;

    @Before
    public void before() {
        Script script = new Script();
        script.setAliasName("xxxx");
        script.setAppPackage("xxxx");
        script.setLn(DB.ScriptLN.GO);
        script.setPath("xxxx");
        script.setDescription("Desc");

        script.setCreateBy(7L);
        script.setCreateTime(new Date());
        script.setEnabled(true);

        mapper.insert(script);

        this.entity = script;
    }

    @Test
    public void testSelectById() {
        Script entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<Script> entityList = mapper.selectListByProperty(Script::getLn, this.entity.getLn());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        ScriptCondition condition = new ScriptCondition();
        condition.setLn(DB.ScriptLN.GO);
        List<Script> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        String des = "xxxxx";
        Script entity1 = mapper.selectById(this.entity.getId());
        entity1.setDescription(des);
        mapper.update(entity1);
        Script entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getDescription(), des);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        Script entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        ScriptCondition condition = new ScriptCondition();
        condition.setLn(DB.ScriptLN.GO);
        Long count = mapper.count(condition);
        List<Script> entityList = mapper.selectByCondition(condition);
        Assert.assertEquals(count, Long.valueOf(entityList.size()));
    }



}
