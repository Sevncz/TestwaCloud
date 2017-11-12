package com.testwa.distest.server.service;


import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.form.RequestListBase;
import com.testwa.distest.server.service.task.form.TaskSceneListForm;
import com.testwa.distest.server.service.task.form.TaskSceneNewForm;
import com.testwa.distest.server.service.task.form.TaskSceneUpdateForm;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class TaskSceneServiceTest {

    @Autowired
    private TaskSceneService taskSceneService;

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testSave(){
        TaskSceneNewForm form = new TaskSceneNewForm();
        form.setAppId(4l);
        form.setCaseIds(Arrays.asList(2l, 1l, 3l));
        form.setProjectId(4l);
        form.setSceneName("测试场景");
        taskSceneService.save(form);
    }

    @Test
    public void testDelete(){
        taskSceneService.delete(3l);
    }
    @Test
    public void testDeleteAll(){
        taskSceneService.delete(Arrays.asList(6l, 5l));
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpdate(){
        TaskSceneUpdateForm form = new TaskSceneUpdateForm();
        form.setTaskSceneId(4l);
        form.setAppId(5l);
        form.setCaseIds(Arrays.asList(3l,2l, 1l));
        taskSceneService.update(form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindPageForCurrentUser(){
        TaskSceneListForm form = new TaskSceneListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        taskSceneService.findPageForCurrentUser(form);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindForCurrentUser(){
        TaskSceneListForm form = new TaskSceneListForm();
        form.setProjectId(4l);
        taskSceneService.findForCurrentUser(form);
    }

    @Test
    public void testFind(){
        TaskSceneListForm form = new TaskSceneListForm();
        form.setProjectId(4l);
        taskSceneService.find(form);
    }
    @Test
    public void testFindPage(){
        TaskSceneListForm form = new TaskSceneListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(1);
        page.setPageSize(10);
        form.setPage(page);
        form.setProjectId(4l);
        taskSceneService.findByPage(form);
    }

    @Test
    public void testGetTaskSceneVO(){
        taskSceneService.getTaskSceneVO(4l);
    }

}
