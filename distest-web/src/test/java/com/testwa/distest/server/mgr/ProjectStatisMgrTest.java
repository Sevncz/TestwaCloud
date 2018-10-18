package com.testwa.distest.server.mgr;


import com.testwa.core.base.vo.PageResultVO;
import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.web.project.mgr.ProjectStatisMgr;
import com.testwa.distest.server.web.project.vo.ProjectStatisMultiBarVO;
import com.testwa.distest.server.web.project.vo.ProjectStatisTestInfoVO;
import com.testwa.distest.server.web.project.vo.ProjectTestDynamicVO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-dev.properties")
public class ProjectStatisMgrTest {

    @Autowired
    private ProjectStatisMgr projectStatisMgr;

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testStatisAppTestCountForEveryTestType(){

        long projectId = 15;
        Date lastDay = DateTime.now().plusDays(-1).toDate();
        long startTime = lastDay.getTime() / 1000;
        long endTime = System.currentTimeMillis() / 1000;

        ProjectStatisMultiBarVO vo = projectStatisMgr.statisAppTestCountForEveryTestType(projectId, startTime, endTime);

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testStatisMemberTestCountForEveryTestType(){

        long projectId = 15;
        Date lastDay = DateTime.now().plusDays(-1).toDate();
        long startTime = lastDay.getTime() / 1000;
        long endTime = System.currentTimeMillis() / 1000;

        ProjectStatisMultiBarVO vo = projectStatisMgr.statisMemberTestCountForEveryTestType(projectId, startTime, endTime);

    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testStatisTestInfo(){

        long projectId = 15;
        Date lastDay = DateTime.now().plusDays(-1).toDate();
        long startTime = lastDay.getTime() / 1000;
        long endTime = System.currentTimeMillis() / 1000;

        ProjectStatisTestInfoVO vo = projectStatisMgr.statisTestInfo(projectId, startTime, endTime);
        log.info(vo.toString());
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testDynamicTestPage(){

        long projectId = 15;
        Date lastDay = DateTime.now().plusDays(-1).toDate();
        long startTime = lastDay.getTime() / 1000;
        long endTime = System.currentTimeMillis() / 1000;
        TaskListForm form = new TaskListForm();
        PageResultVO<ProjectTestDynamicVO> voPageResult = projectStatisMgr.dynamicTestPage(projectId, startTime, endTime, form);
        voPageResult.getPages().forEach( v -> {
            log.info("" + v.getTime());
        });
    }

}
