package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.entity.Postman;
import com.testwa.distest.server.service.apitest.form.CategoryNewForm;
import com.testwa.distest.server.service.apitest.service.ApiCategoryService;
import com.testwa.distest.server.service.apitest.service.ApiService;
import com.testwa.distest.server.service.apitest.service.PostmanService;
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
    private Api api;
    private ApiCategory category;

    @Autowired
    private ApiService apiService;
    @Autowired
    private ApiCategoryService apiCategoryService;

    @Before
    public void before() {

        Long projectId = 1L;
        Long parentId = 0L;
        CategoryNewForm form = new CategoryNewForm();
        form.setDescription("xxxx");
        form.setName("test");
        this.category = apiCategoryService.save(projectId, parentId, form);

        this.api = save(projectId, this.category.getId());
    }

    protected Api save(Long projectId, Long categoryId) {
        String url = "http://api.testwa.com/";
        String method = "GET";
        List<Map<String, String>> param = new ArrayList<>();
        Map<String, String> authorization = new HashMap<>();
        List<Map<String, String>> header = new ArrayList<>();
        Map<String, String> body = new HashMap<>();
        String script = "";
        String preScript = "";
        String description = "";
        String apiName = "";

        return apiService.save(projectId, categoryId, apiName, url, method, param, authorization, header, body, preScript, script, description);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testSave() {
        Long projectId = 1L;
        Long categoryId = this.category.getId();
        save(projectId, categoryId);

        Api api1 = apiService.get(api.getId());
        Assert.assertNotNull(api1);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testListByCategoryId() {

        Long projectId = 1L;
        Long categoryId = this.category.getId();
        String url = "http://api.testwa.com/";
        String method = "GET";
        List<Map<String, String>> param = new ArrayList<>();
        Map<String, String> authorization = new HashMap<>();
        List<Map<String, String>> header = new ArrayList<>();
        Map<String, String> body = new HashMap<>();
        String script = "";
        String preScript = "";
        String description = "";
        String apiName = "";

        int loopTime = 30;
        for (int i=0;i<loopTime;i++) {
            apiService.save(projectId, categoryId, apiName, url, method, param, authorization, header, body, preScript, script, description);
        }

        List<Api> apiList = apiService.listByCategoryId(categoryId);
        Assert.assertFalse(apiList.isEmpty());
        Assert.assertTrue(apiList.size() >= loopTime);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdateCategory() {
        Long categoryId = 1000L;

        apiService.updateCategory(this.api.getId(), categoryId);
        Api api = apiService.get(this.api.getId());
        Assert.assertEquals(api.getCategoryId(), categoryId);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDisable() {
        apiService.disable(this.api.getId());
        Api api = apiService.get(this.api.getId());
        Assert.assertNull(api);
    }

}
