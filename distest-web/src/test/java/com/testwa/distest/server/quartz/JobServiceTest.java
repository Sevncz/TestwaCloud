package com.testwa.distest.server.quartz;

import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.quartz.JobInfoVO;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.quartz.service.JobService;
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
@TestPropertySource(locations="classpath:application-test.properties")
public class JobServiceTest {

    @Autowired
    private JobService jobService;


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testAddJob(){
        try {
            jobService.addJob("com.testwa.distest.quartz.job.TestJob", "test", "*/10 * * * * ?", "测试一下");
        } catch (BusinessException e) {
            e.printStackTrace();
        }

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testList(){
        PageResult<JobInfoVO> vos = jobService.list(1, 10);
        vos.getPages().forEach(task -> {
            log.info(task.toString());
        });

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testTrigger(){
        try {
            jobService.trigger("com.testwa.distest.quartz.job.TestJob", "test");
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }


    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDelete(){
        try {
            jobService.delete("com.testwa.distest.quartz.TestJob", "test");
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

}
