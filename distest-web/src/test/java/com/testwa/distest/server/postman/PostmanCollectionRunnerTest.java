package com.testwa.distest.server.postman;


import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.postman.PostmanCollectionRunner;
import com.testwa.distest.postman.PostmanRunResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class PostmanCollectionRunnerTest {

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testRunCollection(){
        String envFilename = "/Users/wen/Dropbox/Distest/接口文档/distest线上环境.postman_environment.json";
        String colFilename = "/Users/wen/Dropbox/Distest/接口文档/testCollection.postman_collection.json";
        String folder = null;

        PostmanCollectionRunner runner = new PostmanCollectionRunner();
        try {
            runner.init(colFilename, envFilename);
            PostmanRunResult result = runner.runCollection(folder, false);
            log.info(result.toString());
        } catch (Exception e) {

        }
    }

}
