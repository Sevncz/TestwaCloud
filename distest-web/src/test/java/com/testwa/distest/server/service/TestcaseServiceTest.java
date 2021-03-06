package com.testwa.distest.server.service;

import com.testwa.distest.DistestWebApplication;
import com.testwa.core.base.form.RequestListBase;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.service.testcase.form.TestcaseListForm;
import com.testwa.distest.server.service.testcase.form.TestcaseNewForm;
import com.testwa.distest.server.service.testcase.form.TestcaseUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class TestcaseServiceTest {

    @Autowired
    private TestcaseService testcaseService;

    @Test
    @WithMockUser(username = "xiaoming", authorities = { "ADMIN", "USER" })
    public void testSave(){
        TestcaseNewForm form = new TestcaseNewForm();
        form.setDescription("测试案例啊啊啊啊啊啊啊");
        form.setName("测试案例测试啊");
        form.setScriptIds(Arrays.asList(2l, 3l));
//        testcaseService.saveFunctionalTestcase(15L, appInfo, form);
    }

    @Test
    public void testFindPage(){
        TestcaseListForm form = new TestcaseListForm();
        form.setPageNo(1);
        form.setPageSize(10);
        testcaseService.findPage(15L, form);
    }

    @Test
    public void testFindOne(){
        testcaseService.get(1l);
    }

    @Test
    public void testFindAll(){
        testcaseService.findAll(Arrays.asList(1l, 2l, 3l));
    }

    @Test
    public void testFindAllOrder(){
        List<Testcase> testcaseList = testcaseService.findByCaseOrder(Arrays.asList(2l, 3l, 1l));
        System.out.println(testcaseList.toString());
    }

    @Test
    public void testDelete(){
        testcaseService.delete(4l);
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
    public void testSaveAndDelete(){
        TestcaseNewForm form = new TestcaseNewForm();
        form.setDescription("测试案例啊啊啊啊啊啊啊");
        form.setName("测试案例测试啊");
        form.setScriptIds(Arrays.asList(2l, 3l, 1l));
//        Long testcaseId = testcaseService.saveFunctionalTestcase(15L, appInfo, form);

//        testcaseService.deleteByIds(testcaseId);
    }

    @Test
    public void testCountByProject(){
        testcaseService.countByProject(4l);
    }

    @Test
    public void testUpdate(){
        Testcase testcase = testcaseService.get(1l);
        TestcaseUpdateForm form = new TestcaseUpdateForm();
        form.setTestcaseId(testcase.getId());
        form.setDescription("update lo");
        form.setName(testcase.getCaseName());
        form.setScriptIds(Arrays.asList(1l, 3l));
        testcaseService.update(form);
    }

}
