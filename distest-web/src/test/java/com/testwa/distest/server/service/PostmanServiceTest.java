package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Postman;
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

/**
 * @author wen
 * @create 2018-12-04 17:41
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class PostmanServiceTest {

    @Autowired
    private PostmanService postmanService;

    @Test
    @Transactional
    @Rollback
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testSavePostmanCollection() {
        Long projectId = 1L;

        String collectionPath = "/Users/wen/Dropbox/Distest/接口文档/testCollection.postman_collection.json";
        String postmanId = "0993071f-1144-4423-82ef-7bb841cb272b";
        long id = postmanService.save(projectId, collectionPath, postmanId);

        Postman postman = postmanService.get(id);
        Assert.assertNotNull(postman);
    }

}
