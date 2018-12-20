package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.ProjectCondition;
import com.testwa.distest.server.condition.ScriptCondition;
import com.testwa.distest.server.entity.Project;
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

import java.util.*;

/**
 * @author wen
 * @create 2018-12-20 10:47
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class ProjectMapperTest {

    private Project entity;

    @Autowired
    private ProjectMapper mapper;

    @Before
    public void before() {
        Project project = new Project();
        project.setProjectName("测试项目");
        project.setDescription("aaaaaa");

        project.setCreateBy(7L);
        project.setCreateTime(new Date());
        project.setEnabled(true);

        mapper.insert(project);

        this.entity = project;
    }


    @Test
    public void testSelectById() {
        Project entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<Project> entityList = mapper.selectListByProperty(Project::getCreateBy, this.entity.getCreateBy());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        ProjectCondition condition = new ProjectCondition();
        condition.setProjectName("%测%");
        List<Project> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        String des = "xxxxx";
        Project entity1 = mapper.selectById(this.entity.getId());
        entity1.setDescription(des);
        mapper.update(entity1);
        Project entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getDescription(), des);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        Project entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        ProjectCondition condition = new ProjectCondition();
        condition.setProjectName("%测%");
        long count = mapper.count(condition);
        List<Project> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= count);
        Assert.assertTrue(count > 0);
    }


}
