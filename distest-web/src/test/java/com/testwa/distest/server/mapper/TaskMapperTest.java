package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.TaskCondition;
import com.testwa.distest.server.condition.TestcaseCondition;
import com.testwa.distest.server.entity.Task;
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
public class TaskMapperTest {

    private Task entity;

    @Autowired
    private TaskMapper mapper;

    @Before
    public void before() {
        Task task = new Task();
        task.setStatus(DB.TaskStatus.RUNNING);
        task.setAppId(1L);
        task.setAppJson("[{}]");
        task.setDevicesJson("[{}]");
        task.setScriptJson("[{}]");
        task.setTestcaseJson("[{}]");
        task.setTaskName("TASKNAME");
        task.setTaskCode(123L);
        task.setTaskType(DB.TaskType.COMPATIBILITY);

        task.setCreateBy(7L);
        task.setCreateTime(new Date());
        task.setEnabled(true);

        mapper.insert(task);

        this.entity = task;
    }

    @Test
    public void testSelectById() {
        Task entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<Task> entityList = mapper.selectListByProperty(Task::getCreateBy, this.entity.getCreateBy());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        TestcaseCondition condition = new TestcaseCondition();
        condition.setAppName("%APP%");
        List<Task> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        DB.TaskStatus status = DB.TaskStatus.CANCEL;
        Task entity1 = mapper.selectById(this.entity.getId());
        entity1.setStatus(status);
        mapper.update(entity1);
        Task entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getStatus(), status);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        Task entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        TaskCondition condition = new TaskCondition();
        condition.setStatus(this.entity.getStatus());
        Long count = mapper.count(condition);
        List<Task> entityList = mapper.selectByCondition(condition);
        Assert.assertEquals(count, Long.valueOf(entityList.size()));
    }



}
