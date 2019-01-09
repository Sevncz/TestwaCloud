package com.testwa.distest.server.mapper;

import com.testwa.distest.DistestWebApplication;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.IssueOperationLog;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-20 10:47
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DistestWebApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@Transactional
public class IssueOperationLogMapperTest {

    private IssueOperationLog entity;

    @Autowired
    private IssueOperationLogMapper mapper;

    @Before
    public void before() {
        IssueOperationLog issueOperationLog = new IssueOperationLog();
        issueOperationLog.setIssueId(1L);
        issueOperationLog.setOpType(DB.IssueOpTypeEnum.ADD);

        issueOperationLog.setUserId(7L);
        issueOperationLog.setCreateTime(new Date());
        issueOperationLog.setEnabled(true);

        mapper.insert(issueOperationLog);

        this.entity = issueOperationLog;
    }

    @Test
    public void testSelectById() {
        IssueOperationLog entity1 = mapper.selectById(this.entity.getId());
        Assert.assertNotNull(entity1);
    }

    @Test
    public void testSelectListByProperty() {
        List<IssueOperationLog> entityList = mapper.selectListByProperty(IssueOperationLog::getIssueId, 1L);
        Assert.assertTrue(entityList.size() >= 1);
    }

}
