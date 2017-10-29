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
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.form.TaskNewForm;
import com.testwa.distest.server.service.task.form.TaskUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.task.vo.TaskVO;
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

    public TaskScene findOne(Long taskId) {
        return taskSceneDAO.findOne(taskId);
    }

    public void delete(Long taskId){
        taskSceneDAO.delete(taskId);
    }

    /**
     * 用户可见的任务列表
     * @param queryForm
     * @return
     */
    public List<TaskScene> find(TaskListForm queryForm) {
        Map<String, Object> params = buildQueryParams(queryForm);
        return taskSceneDAO.findByFromProject(params);
    }

    /**
     * 用户可见的任务分页列表
     * @param pageForm
     * @return
     */
    public PageResult<TaskScene> findByPage(TaskListForm pageForm) {
        Map<String, Object> params = buildQueryParams(pageForm);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<TaskScene> entityList = taskSceneDAO.findByFromProject(params);
        PageInfo<TaskScene> info = new PageInfo(entityList);
        PageResult<TaskScene> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    private Map<String, Object> buildQueryParams(TaskListForm queryForm) {
        List<Project> projects = projectService.findAllOfUserProject(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", queryForm.getProjectId());
        params.put("taskName", queryForm.getTaskName());
        params.put("projects", projects);
        return params;
    }


    public TaskVO getTaskVO(String taskId) {
        TaskScene task = taskSceneDAO.findOne(taskId);
        TaskVO taskVO = new TaskVO();
        BeanUtils.copyProperties(task, taskVO);

        //get app
        taskVO.setApp(appService.getAppVO(task.getAppId()));

        List<TestcaseVO> testcaseVOs = new ArrayList<>();
        task.getTestcases().forEach(testcase -> {
            TestcaseVO testcaseVO = new TestcaseVO();
            BeanUtils.copyProperties(testcase, testcaseVO );
            testcaseVOs.add(testcaseVO);
        });
        taskVO.setTestcases(testcaseVOs);
        return taskVO;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public void update(TaskUpdateForm updateForm) throws NoSuchTaskException, NoSuchTestcaseException,NoSuchAppException {
        TaskScene task = findOne(updateForm.getTaskId());
        if (task == null) {
            throw new NoSuchTaskException("无此任务！");
        }
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        List<Long> caseIds = updateForm.getCaseIds();
        appService.checkApp(updateForm.getAppId());
        task.setAppId(updateForm.getAppId());
//        task.setTestcaseIds(caseIds);
        task.setDescription(updateForm.getDescription());
        task.setUpdateBy(user.getId());
        task.setTaskName(updateForm.getTaskName());
        taskSceneDAO.update(task);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long save(TaskNewForm form) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        List<Long> caseIds = form.getCaseIds();

        TaskScene task = new TaskScene();
        task.setAppId(form.getAppId());
        task.setTaskName(form.getTaskName());
        task.setProjectId(form.getProjectId());
        task.setDescription(form.getDescription());
        task.setCreateTime(new Date());
        task.setCreateBy(user.getId());
        task.setExeMode(DB.RunMode.COMMONTEST);

        long taskId = save(task);

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
