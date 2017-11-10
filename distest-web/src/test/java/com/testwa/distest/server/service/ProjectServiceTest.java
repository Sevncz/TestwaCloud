package com.testwa.distest.server.service;

import com.testwa.distest.WebServerApplication;
import com.testwa.distest.common.form.RequestListBase;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.user.service.UserService;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WebServerApplication.class)
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @Test
    public void testInsert(){
        User user = userService.findByUsername("xiaoming");

        long countBefore =projectService.count();
        Project project = new Project();
        project.setProjectName("test project " + RandomUtils.nextInt(1, 50));
        project.setCreateBy(user.getId());
        projectService.insert(project);
        long countAfter =projectService.count();
        Assert.assertEquals(++countBefore,countAfter);
    }

    @Test
    public void testFind(){
        Project project = projectService.findOne(1l);
        System.out.println(project.toString());
    }
    @Test
    public void testFindByPage(){
        ProjectListForm form = new ProjectListForm();
        RequestListBase.Page page = form.getPage();
        page.setPageNo(3);
        page.setPageSize(10);
        form.setPage(page);
        PageResult<Project> projects = projectService.findByPage(form);
    }
    @Test
    public void testFindAllOfUserProject(){
        List<Project> projects = projectService.findAllOfUserProject("xiaoming");
    }
    @Test
    public void testDeleteOne(){
        List<Project> projects = projectService.findAll();
        if(projects.size() > 0){
            projectService.delete(projects.get(0).getId());
        }
    }

    @Test
    public void testDeleteAll(){
        List<Project> projects = projectService.findAll();
        List<Long> ids = new ArrayList<>();
        projects.forEach(p -> {
            ids.add(p.getId());
        });
        projectService.deleteAll(ids);
    }

    @Test
    public void getProjectCountByUser(){
        User user = userService.findByUsername("xiaoming");
        long count = projectService.getProjectCountByOwner(user.getId());
        List<Project> projects = projectService.findAllOfUserProject(user.getUsername());
        Assert.assertEquals(count, projects.size());
    }

}
