package com.testwa.distest.server.service;


import com.testwa.distest.DistestWebApplication;
import com.testwa.core.base.form.RequestListBase;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.form.ScriptNewForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import org.junit.Assert;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class ScriptServiceTest {

    @Autowired
    private ScriptService scriptService;

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/PHLink/测试app和脚本/android_ui_monkey.py");
        MockMultipartFile file = new MockMultipartFile("file", "android_ui_monkey.py", "multipart/form-data", inputFile);
        scriptService.upload(file);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testNewAndUpload() throws IOException {
        FileInputStream inputFile = new FileInputStream( "/Users/wen/Documents/PHLink/测试app和脚本/ContactManager_Android_02282215.py");
        MockMultipartFile file = new MockMultipartFile("file", "ContactManager_Android_02282215.py", "multipart/form-data", inputFile);
        scriptService.upload(file, 15L);
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
        form.setPageNo(1);
        form.setPageSize(10);
        scriptService.findPage(15L, form);
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

    @Test
    public void testResovlePyPacakge() {
        String l = "desired_caps['appPackage'] ='com.orion.xiaoya.speakerclient'";
        String packageName = scriptService.resovlePyPacakge(l);
        Assert.assertEquals(packageName, "com.orion.xiaoya.speakerclient");
    }

}
