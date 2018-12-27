package com.testwa.distest.server.mapper;

import com.alibaba.fastjson.JSON;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
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
public class ApiMapperTest {

    private Api api;

    @Autowired
    private ApiMapper apiMapper;

    @Before
    public void before() {
        Long projectId = 1L;
        String url = "http://api.testwa.com/";
        String method = "GET";
        List<Map<String, String>> param = new ArrayList<>();
        Map<String, String> authorization = new HashMap<>();
        List<Map<String, String>> header = new ArrayList<>();
        Map<String, String> body = new HashMap<>();
        String script = "";
        String preScript = "";
        String description = "";

        Api api = new Api();
        api.setProjectId(projectId);
        api.setUrl(url);
        api.setMethod(method);
        api.setParam(JSON.toJSONString(param));
        api.setAuthorization(JSON.toJSONString(authorization));
        api.setHeader(JSON.toJSONString(header));
        api.setBody(JSON.toJSONString(body));
        api.setPreScript(preScript);
        api.setScript(script);
        api.setDescription(description);
        api.setCategoryPath("1,2,3,4,5,6");

        api.setCreateBy(7L);
        api.setCreateTime(new Date());
        api.setEnabled(true);

        apiMapper.insert(api);

        this.api = api;
    }


    @Test
    public void testSelectById() {
        Api api1 = apiMapper.selectById(this.api.getId());
        Assert.assertNotNull(api1);
    }

    @Test
    public void testUpdate() {
        String des = "xxxxx";
        Api api1 = apiMapper.selectById(this.api.getId());
        api1.setDescription(des);
        apiMapper.update(api1);
        Api api2 = apiMapper.selectById(this.api.getId());
        Assert.assertEquals(api2.getDescription(), des);
    }

    @Test
    public void testDelete() {
        int line = apiMapper.delete(this.api.getId());
        Assert.assertEquals(line, 1);
        Api api2 = apiMapper.selectById(this.api.getId());
        Assert.assertNull(api2);
    }



}
