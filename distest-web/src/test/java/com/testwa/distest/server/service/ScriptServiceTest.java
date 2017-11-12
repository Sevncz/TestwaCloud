package com.testwa.distest.server.service;


import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.form.RequestListBase;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptNewForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class ScriptServiceTest {

    @Autowired
    private ScriptService scriptService;

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/testwa/testapp/ContactManager_Android_02282215.py");
        MockMultipartFile file = new MockMultipartFile("file", "ContactManager_Android_02282215.py", "multipart/form-data", inputFile);
        scriptService.upload(file);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testNewAndUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/testwa/testapp/ContactManager_Android_02282215.py");
        MockMultipartFile file = new MockMultipartFile("file", "ContactManager_Android_02282215.py", "multipart/form-data", inputFile);
        ScriptNewForm form = new ScriptNewForm();
        form.setProjectId(4l);
        form.setDescription("描述啊描述");
        form.setTag("NEW");
        scriptService.upload(file, form);
    }

    @Test
    public void testFindOne(){
        scriptService.findOne(1l);
    }

    @Test
    public void testFindAll(){
        scriptService.findAll(Arrays.asList(1l, 2l));
    }

    @Test
    public void testFindPage(){
        ScriptListForm form = new ScriptListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        scriptService.findPage(form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindPageForCurrentUser(){
        ScriptListForm form = new ScriptListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        scriptService.findPageForCurrentUser(form);
    }
    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindForCurrentUser(){
        ScriptListForm form = new ScriptListForm();
        scriptService.findForCurrentUser(form);
    }

    @Test
    public void testFindAllByTestcaseId(){
        scriptService.findAllByTestcaseId(1l);
    }

    @Test
    public void testGetContent() throws IOException {
        String content = scriptService.getContent(1l);
        System.out.println(content);
    }
    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testModifyContent() throws IOException {
        scriptService.modifyContent(1l, "test");
    }

}
