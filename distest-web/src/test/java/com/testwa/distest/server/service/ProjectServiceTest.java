package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.User;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.service.project.form.ProjectListForm;
import com.testwa.distest.server.service.project.form.ProjectUpdateForm;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.user.service.UserService;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
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
        Project project = projectService.get(4l);
        System.out.println(project.toString());
    }
    @Test
    public void testFindByPage(){
        ProjectListForm form = new ProjectListForm();
        form.setPageNo(3);
        form.setPageSize(10);
        PageResult<Project> projects = projectService.page(form);
    }
    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testFindAllOfUserProject(){
        List<Project> projects = projectService.listByUser(7L);
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testDeleteOne(){
        List<Project> projects = projectService.list();
        if(!projects.isEmpty()){
            projectService.delete(projects.get(0).getId());
        }
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testDeleteAll(){
        List<Project> projects = projectService.list();
        List<Long> ids = new ArrayList<>();
        projects.forEach(p -> {
            ids.add(p.getId());
        });
        projectService.delete(ids);
    }

    @Test
    public void testGetProjectCountByUser(){
        User user = userService.findByUsername("xiaoming");
        long count = projectService.getProjectCountByOwner(user.getId());
        List<Project> projects = projectService.listByUser(7L);
        Assert.assertEquals(count, projects.size());
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testGetRecentViewProject(){
        try {
            projectService.getRecentViewProject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testUpdate(){
        try {
            ProjectUpdateForm form = new ProjectUpdateForm();
            List<Project> projects = projectService.list();
            if(!projects.isEmpty()){
                form.setProjectId(projects.get(0).getId());
                form.setProjectName("hhhhha");
                projectService.update(form);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
