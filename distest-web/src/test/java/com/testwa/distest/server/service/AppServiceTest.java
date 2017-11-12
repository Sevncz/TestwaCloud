package com.testwa.distest.server.service;


import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.distest.common.exception.NoSuchAppException;
import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.form.AppNewForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.project.service.ProjectService;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class AppServiceTest {

    @Autowired
    private AppService appService;
    @Autowired
    private ProjectService projectService;


    @Test
    public void testInsert(){
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/testwa/testapp/ContactManager.apk");
        MockMultipartFile file = new MockMultipartFile("file", "ContactManager.apk", "multipart/form-data", inputFile);
        AppNewForm form = new AppNewForm();
        form.setDescription("hhhhh");
        form.setProjectId(4l);
        form.setVersion("1.0");
        appService.upload(file, form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpdate(){
        AppUpdateForm form = new AppUpdateForm();
        form.setAppId(1l);
        form.setDescription("xxx测试一下");
        form.setVersion("V0.2");
        appService.update(form);
    }

    @Test
    public void testDelete(){
        appService.delete(1l);
    }

    @Test
    public void testFindOne(){
        App app = appService.findOne(1l);
    }

}
