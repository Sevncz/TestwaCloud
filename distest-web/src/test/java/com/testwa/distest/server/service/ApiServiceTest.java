package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.Postman;
import com.testwa.distest.server.service.apitest.service.ApiService;
import com.testwa.distest.server.service.apitest.service.PostmanService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wen
 * @create 2018-12-04 17:41
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class ApiServiceTest {

    @Autowired
    private ApiService apiService;

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testSave() {
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

        long id = apiService.save(projectId, url, method, param, authorization, header, body, preScript, script, description);

        Api api = apiService.get(id);
        Assert.assertNotNull(api);
    }

}
