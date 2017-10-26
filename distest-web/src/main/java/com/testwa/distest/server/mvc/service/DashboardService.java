package com.testwa.distest.server.mvc.service;

import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.server.mvc.vo.QuickDeployVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 16/9/7.
 */
@Slf4j
@Service
public class DashboardService extends BaseService{
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;

    public String quickDeploy(User user, QuickDeployVO quickDeployVO) throws NoSuchScriptException, NoSuchProjectException, Exception {
//        create case
        String caseId = testcaseService.createCaseQuick(quickDeployVO.getProjectId(), user, quickDeployVO.getScripts());
        // create task
        return taskService.createTaskQuickAndDeploy(quickDeployVO.getProjectId(), user, quickDeployVO.getAppId(), caseId, quickDeployVO.getDevices());
    }
}
