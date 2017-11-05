package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.common.enums.DB;
import com.testwa.distest.common.exception.NoSuchAppException;
import com.testwa.distest.common.exception.NoSuchTaskException;
import com.testwa.distest.common.exception.NoSuchTestcaseException;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.core.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskSceneDAO;
import com.testwa.distest.server.service.task.dao.ITaskTestcaseDAO;
import com.testwa.distest.server.service.task.form.TaskSceneListForm;
import com.testwa.distest.server.service.task.form.TaskSceneNewForm;
import com.testwa.distest.server.service.task.form.TaskSceneUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.task.vo.TaskSceneVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

/**
 * Created by wen on 24/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskSceneService {


    @Autowired
    private ITaskSceneDAO taskSceneDAO;
    @Autowired
    private ITaskTestcaseDAO taskTestcaseDAO;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private ProjectService projectService;

    public long save(TaskScene task){
        return taskSceneDAO.insert(task);
    }

    public TaskScene findOne(Long entityId) {
        return taskSceneDAO.findOne(entityId);
    }

    public void delete(Long entityId){
        taskSceneDAO.delete(entityId);
    }

    /**
     * 用户可见的任务列表
     * @param queryForm
     * @return
     */
    public List<TaskScene> find(TaskSceneListForm queryForm) {
        Map<String, Object> params = buildQueryParams(queryForm);
        return taskSceneDAO.findByFromProject(params);
    }

    /**
     * 用户可见的任务分页列表
     * @param pageForm
     * @return
     */
    public PageResult<TaskScene> findByPage(TaskSceneListForm pageForm) {
        Map<String, Object> params = buildQueryParams(pageForm);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<TaskScene> entityList = taskSceneDAO.findByFromProject(params);
        PageInfo<TaskScene> info = new PageInfo(entityList);
        PageResult<TaskScene> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    private Map<String, Object> buildQueryParams(TaskSceneListForm queryForm) {
        List<Project> projects = projectService.findAllOfUserProject(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", queryForm.getProjectId());
        params.put("taskName", queryForm.getTaskName());
        params.put("projects", projects);
        return params;
    }


    public TaskSceneVO getTaskSceneVO(String taskSceneId) {
        TaskScene ts = taskSceneDAO.findOne(taskSceneId);
        TaskSceneVO taskVO = new TaskSceneVO();
        BeanUtils.copyProperties(ts, taskVO);

        //get app
        taskVO.setApp(appService.getAppVO(ts.getAppId()));

        List<TestcaseVO> testcaseVOs = new ArrayList<>();
        ts.getTestcases().forEach(testcase -> {
            TestcaseVO testcaseVO = new TestcaseVO();
            BeanUtils.copyProperties(testcase, testcaseVO );
            testcaseVOs.add(testcaseVO);
        });
        taskVO.setTestcases(testcaseVOs);
        return taskVO;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void update(TaskSceneUpdateForm updateForm) throws NoSuchTaskException, NoSuchTestcaseException,NoSuchAppException {
        TaskScene ts = findOne(updateForm.getTaskSceneId());
        if (ts == null) {
            throw new NoSuchTaskException("无此任务！");
        }
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        List<Long> caseIds = updateForm.getCaseIds();
        appService.checkApp(updateForm.getAppId());
        ts.setAppId(updateForm.getAppId());
//        ts.setTestcaseIds(caseIds);
        ts.setDescription(updateForm.getDescription());
        ts.setUpdateBy(user.getId());
        ts.setTaskName(updateForm.getTaskName());
        taskSceneDAO.update(ts);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long save(TaskSceneNewForm form) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        List<Long> caseIds = form.getCaseIds();

        TaskScene ts = new TaskScene();
        ts.setAppId(form.getAppId());
        ts.setTaskName(form.getTaskName());
        ts.setProjectId(form.getProjectId());
        ts.setDescription(form.getDescription());
        ts.setCreateTime(new Date());
        ts.setCreateBy(user.getId());
        ts.setExeMode(DB.RunMode.COMMONTEST);

        long taskId = save(ts);

        insertAllTaskTestcase(caseIds, taskId);
        return taskId;
    }

    private void insertAllTaskTestcase(List<Long> caseIds, long taskId) {
        List<TaskTestcase> allTestcase = new ArrayList<>();
        for(int i=0;i<caseIds.size();i++){

            TaskTestcase tt = new TaskTestcase();
            tt.setTaskId(taskId);
            tt.setSeq(i);
            tt.setTestcaseId(caseIds.get(i));
            allTestcase.add(tt);
        }
        taskTestcaseDAO.insertAll(allTestcase);
    }

    public List<TaskScene> findAll(List<Long> entityIds) {
        return taskSceneDAO.findAll(entityIds);
    }


    public int delete(List<Long> entityIds) {
        return taskSceneDAO.delete(entityIds);
    }
}
