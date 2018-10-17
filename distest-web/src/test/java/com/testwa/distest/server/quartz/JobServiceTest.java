package com.testwa.distest.server.quartz;

import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.quartz.TaskInfoVo;
import com.testwa.distest.quartz.exception.BusinessException;
import com.testwa.distest.quartz.service.JobService;
import com.testwa.distest.server.web.project.mgr.ProjectStatisMgr;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class JobServiceTest {

    @Autowired
    private JobService jobService;


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testAddJob(){
        try {
            jobService.addJob("com.testwa.distest.quartz.TestJob", "test", "*/10 * * * * ?", "测试一下");
        } catch (BusinessException e) {
            e.printStackTrace();
        }

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testList(){
        PageResult<TaskInfoVo> vos = jobService.list(1, 10);
        vos.getPages().forEach(task -> {
            log.info(task.toString());
        });

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testTrigger(){
        try {
            jobService.trigger("com.testwa.distest.quartz.TestJob", "test");
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

}
