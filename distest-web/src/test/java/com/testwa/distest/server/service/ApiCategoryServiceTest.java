package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.service.apitest.form.CategoryNewForm;
import com.testwa.distest.server.service.apitest.service.ApiCategoryService;
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
public class ApiCategoryServiceTest {
    private ApiCategory category;

    @Autowired
    private ApiCategoryService apiCategoryService;

    @Before
    public void before() {
        Long projectId = 1L;
        Long parentId = 1L;
        CategoryNewForm form = new CategoryNewForm();
        form.setDescription("xxxx");
        form.setName("test");
        this.category = apiCategoryService.save(projectId, parentId, form);
    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testSave() {
        Long projectId = 1L;
        Long parentId = 1L;
        CategoryNewForm form = new CategoryNewForm();
        form.setDescription("xxxx");
        form.setName("test");
        ApiCategory category = apiCategoryService.save(projectId, parentId, form);

        ApiCategory category1 = apiCategoryService.get(category.getId());

        Assert.assertNotNull(category1);

    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdate() {
        CategoryNewForm form = new CategoryNewForm();
        form.setDescription("xxxx");
        form.setName("testaaaa");
        apiCategoryService.edit(this.category.getId(), form);

        ApiCategory category1 = apiCategoryService.get(this.category.getId());

        Assert.assertEquals(category1.getCategoryName(), form.getName());
        Assert.assertEquals(category1.getDescription(), form.getDescription());
        Assert.assertEquals(category1.getPreScript(), form.getPreScript());

    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpdateParent() {
        Long newParentId = 3L;
        apiCategoryService.updateParent(this.category.getId(), newParentId);

        ApiCategory category1 = apiCategoryService.get(this.category.getId());

        Assert.assertEquals(category1.getParentId(), newParentId);

    }

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testRemove() {
        apiCategoryService.remove(this.category.getId());

        ApiCategory category1 = apiCategoryService.get(this.category.getId());

        Assert.assertNull(category1);

    }

}
