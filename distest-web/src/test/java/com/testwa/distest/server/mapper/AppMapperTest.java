package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.condition.AppCondition;
import com.testwa.distest.server.condition.ProjectCondition;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Project;
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
public class AppMapperTest {

    private App entity;

    @Autowired
    private AppMapper mapper;

    @Before
    public void before() {
        App app = new App();
        app.setActivity("xxxx");
        app.setDescription("Desc");
        app.setDisplayName("testApp");
        app.setFileAliasName("fileAliasName");
        app.setFileName("filename");
        app.setIcon("xxxx");
        app.setMd5("md5");
        app.setMiniOSVersion("mini");
        app.setPlatform(DB.PhoneOS.ANDROID);
        app.setVersion("1.0.0");

        app.setCreateBy(7L);
        app.setCreateTime(new Date());
        app.setEnabled(true);

        mapper.insert(app);

        this.entity = app;
    }


    @Test
    public void testSelectById() {
        App entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<App> entityList = mapper.selectListByProperty(App::getMd5, this.entity.getMd5());
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testSelectByCondition() {
        AppCondition condition = new AppCondition();
        condition.setDisplayName("%app%");
        List<App> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= 1);
    }

    @Test
    public void testUpdate() {
        String des = "xxxxx";
        App entity1 = mapper.selectById(this.entity.getId());
        entity1.setDescription(des);
        mapper.update(entity1);
        App entity2 = mapper.selectById(this.entity.getId());
        Assert.assertEquals(entity2.getDescription(), des);
    }

    @Test
    public void testDelete() {
        int line = mapper.delete(this.entity.getId());
        Assert.assertEquals(line, 1);
        App entity2 = mapper.selectById(this.entity.getId());
        Assert.assertNull(entity2);
    }

    @Test
    public void testCount() {
        AppCondition condition = new AppCondition();
        condition.setDisplayName("%app%");
        long count = mapper.count(condition);
        List<App> entityList = mapper.selectByCondition(condition);
        Assert.assertTrue(entityList.size() >= count);
        Assert.assertTrue(count > 0);
    }


}
