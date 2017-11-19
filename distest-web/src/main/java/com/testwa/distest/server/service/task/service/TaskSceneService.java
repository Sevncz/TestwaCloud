package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskSceneDAO;
import com.testwa.distest.server.service.task.dao.ITaskSceneTestcaseDAO;
import com.testwa.distest.server.service.task.form.TaskSceneListForm;
import com.testwa.distest.server.service.task.form.TaskSceneNewForm;
import com.testwa.distest.server.service.task.form.TaskSceneUpdateForm;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.task.vo.TaskSceneVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
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
@Log4j2
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskSceneService {

    @Autowired
    private ITaskSceneDAO taskSceneDAO;
    @Autowired
    private ITaskSceneTestcaseDAO taskSceneTestcaseDAO;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private ProjectService projectService;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(TaskScene task){
        return taskSceneDAO.insert(task);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(TaskSceneUpdateForm updateForm){
        TaskScene ts = findOne(updateForm.getTaskSceneId());
        if(ts == null){
            return;
        }
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        List<Long> caseIds = updateForm.getCaseIds();
        ts.setAppId(updateForm.getAppId());
        ts.setDescription(updateForm.getDescription());
        ts.setUpdateBy(user.getId());
        ts.setSceneName(updateForm.getSceneName());
        taskSceneDAO.update(ts);

        taskSceneTestcaseDAO.deleteByTaskSceneId(updateForm.getTaskSceneId());
        insertAllTaskTestcase(caseIds, updateForm.getTaskSceneId());

    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Long save(TaskSceneNewForm form) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        List<Long> caseIds = form.getCaseIds();

        TaskScene ts = new TaskScene();
        ts.setAppId(form.getAppId());
        ts.setSceneName(form.getSceneName());
        ts.setProjectId(form.getProjectId());
        ts.setDescription(form.getDescription());
        ts.setCreateTime(new Date());
        ts.setCreateBy(user.getId());
        ts.setExeMode(DB.RunMode.REGRESSIONTEST);

        long taskId = save(ts);

        insertAllTaskTestcase(caseIds, taskId);
        return taskId;
    }

    private void insertAllTaskTestcase(List<Long> caseIds, long taskId) {
        List<TaskSceneTestcase> allTestcase = new ArrayList<>();
        for(int i=0;i<caseIds.size();i++){

            TaskSceneTestcase tt = new TaskSceneTestcase();
            tt.setTaskSceneId(taskId);
            tt.setSeq(i);
            tt.setTestcaseId(caseIds.get(i));
            allTestcase.add(tt);
        }
        taskSceneTestcaseDAO.insertAll(allTestcase);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Long entityId){
        // 删除案例中间表记录
        taskSceneTestcaseDAO.deleteByTaskSceneId(entityId);
        // 删除场景记录
        taskSceneDAO.delete(entityId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(List<Long> entityIds) {
        entityIds.forEach(this::delete);
    }

    public TaskScene findOne(Long entityId) {
        return taskSceneDAO.findOne(entityId);
    }

    public List<TaskScene> findAll(List<Long> entityIds) {
        return taskSceneDAO.findAll(entityIds);
    }

    public PageResult<TaskScene> findByPage(TaskSceneListForm pageForm) {
        TaskScene taskScene = new TaskScene();
        taskScene.setSceneName(pageForm.getSceneName());
        taskScene.setProjectId(pageForm.getProjectId());
        taskScene.setAppId(pageForm.getAppId());
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<TaskScene> entityList = taskSceneDAO.findBy(taskScene);
        PageInfo<TaskScene> info = new PageInfo(entityList);
        PageResult<TaskScene> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<TaskScene> find(TaskSceneListForm pageForm) {
        TaskScene taskScene = new TaskScene();
        taskScene.setSceneName(pageForm.getSceneName());
        taskScene.setProjectId(pageForm.getProjectId());
        taskScene.setAppId(pageForm.getAppId());
        List<TaskScene> entityList = taskSceneDAO.findBy(taskScene);
        return entityList;
    }

    /**
     * 用户可见的任务列表
     * @param queryForm
     * @return
     */
    public List<TaskScene> findForCurrentUser(TaskSceneListForm queryForm) {
        Map<String, Object> params = buildQueryParams(queryForm);
        return taskSceneDAO.findByFromProject(params);
    }

    /**
     * 用户可见的任务分页列表
     * @param pageForm
     * @return
     */
    public PageResult<TaskScene> findPageForCurrentUser(TaskSceneListForm pageForm) {
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
        params.put("sceneName", queryForm.getSceneName());
        params.put("projects", projects);
        return params;
    }


    public TaskSceneVO getTaskSceneVO(Long taskSceneId) {
        TaskScene ts = taskSceneDAO.fetchOne(taskSceneId);
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

}
