package com.testwa.distest.server.service;


import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.web.app.vo.AppVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class AppServiceTest {

    @Autowired
    private AppService appService;

    @Test
    public void testInsert(){
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/testwa/testapp/ContactManager.apk");
        MockMultipartFile file = new MockMultipartFile("file", "ContactManager.apk", "multipart/form-data", inputFile);
        appService.upload(file, 15L);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpdate(){
        AppUpdateForm form = new AppUpdateForm();
        form.setAppId(5l);
        form.setDescription("xxx测试一下");
        form.setVersion("V0.2");
        appService.update(form);
    }

    @Test
    public void testDelete(){
        appService.disable(1l);
    }
    @Test
    public void testDeleteApp(){
        appService.deleteApp(3l);
    }

    @Test
    public void testFindOne(){
        App app = appService.get(1l);
    }

    @Test
    public void testFindAll(){
        List<Long> ids = Arrays.asList(4l,5l);
        List<App> apps = appService.findAll(ids);
        log.info(apps.size()+"");
    }
    @Test
    public void testFindPage(){
        AppListForm form = new AppListForm();
        form.setAppName("ContactManager.apk");
        form.setPageNo(1);
        form.setPageSize(10);
        appService.findPage(15L, form);
    }

    @Test
    public void testFindByProjectId(){
        appService.findByProjectId(4l);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testGetAppVO(){
        AppVO vo = appService.getAppVO(4l);
        System.out.println(vo.toString());
    }

}
