package com.testwa.distest.server.service;


import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.distest.WebServerApplication;
import com.testwa.core.base.form.RequestListBase;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.service.app.form.AppListForm;
import com.testwa.distest.server.service.app.form.AppNewForm;
import com.testwa.distest.server.service.app.form.AppUpdateForm;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.web.app.vo.AppVO;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
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
        form.setAppId(5l);
        form.setDescription("xxx测试一下");
        form.setVersion("V0.2");
        appService.update(form);
    }

    @Test
    public void testDelete(){
        appService.delete(1l);
    }
    @Test
    public void testDeleteApp(){
        appService.deleteApp(3l);
    }

    @Test
    public void testFindOne(){
        App app = appService.findOne(1l);
    }

    @Test
    public void testFindAll(){
        List<Long> ids = Arrays.asList(4l,5l);
        List<App> apps = appService.findAll(ids);
        log.info(apps.size());
    }
    @Test
    public void testFindPage(){
        AppListForm form = new AppListForm();
        form.setAppName("ContactManager.apk");
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        appService.findPage(form);
    }

    @Test
    public void testFindByProjectId(){
        appService.findByProjectId(4l);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindPageForCurrentUser() throws ParamsIsNullException {
        AppListForm form = new AppListForm();
        form.setAppName("ContactManager.apk");
        RequestListBase.Page page = form.getPage();
        page.setPageNo(3);
        page.setPageSize(10);
        form.setPage(page);
        appService.findPageForCurrentUser(form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindForCurrentUser(){
        AppListForm form = new AppListForm();
        form.setAppName("ContactManager.apk");
        appService.findForCurrentUser(form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testGetAppVO(){
        AppVO vo = appService.getAppVO(4l);
        System.out.println(vo.toString());
    }

}
