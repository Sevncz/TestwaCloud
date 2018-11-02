package com.testwa.distest.server.service.task.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.repository.AppiumRunningLogRepository;
import com.testwa.distest.server.service.task.dao.ISubTaskDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SubTaskService {

    @Autowired
    private ISubTaskDAO subTaskDAO;
    @Autowired
    private AppiumRunningLogRepository procedureInfoRepository;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(SubTask entity) {
        return subTaskDAO.insert(entity);
    }

    public SubTask findOne(Long entityId) {
        return subTaskDAO.findOne(entityId);
    }

    public List<SubTask> findAll(List<Long> entityIds) {
        return subTaskDAO.findAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(SubTask entity) {
        subTaskDAO.update(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteTaskDevice(List<Long> entityIds) {
        subTaskDAO.delete(entityIds);
        entityIds.forEach( id -> {
            List<AppiumRunningLog> infos = procedureInfoRepository.findByTaskCodeOrderByTimestampAsc(id);
            procedureInfoRepository.delete(infos);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteTaskDevice(Long taskCode) {
        subTaskDAO.disableAll(taskCode);
    }

    public List<SubTask> getRunningtaskDevice(Long projectId, Long userId) {
        SubTask query = new SubTask();
        query.setStatus(DB.TaskStatus.RUNNING);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return subTaskDAO.findBy(query);
    }

    public List<SubTask> getRecentFinishedRunningTask(Long projectId, Long userId) {
        SubTask query = new SubTask();
        query.setStatus(DB.TaskStatus.COMPLETE);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return subTaskDAO.findBy(query);
    }

    public SubTask findOne(Long taskCode, String deviceId) {
        return subTaskDAO.findOne(taskCode, deviceId);
    }

    /**
     *@Description: 按状态顺序返回
     *@Param: [taskCode]
     *@Return: java.util.List<com.testwa.distest.server.entity.SubTask>
     *@Author: wen
     *@Date: 2018/5/3
     */
    public List<SubTask> findByTaskCode(Long taskCode) {
        return subTaskDAO.findByTaskCode(taskCode);
    }

    /**
     *@Description: 返回TaskDevice各个状态的数量
     *@Param: [taskCode]
     *@Return: java.util.Map<java.lang.String,java.lang.Integer>
     *@Author: wen
     *@Date: 2018/5/3
     */
    public List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskCode) {
        return subTaskDAO.countTaskDeviceStatus(taskCode);
    }

    /**
     *@Description: 取消一个设备上的任务
     *@Param: [deviceId, taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/3
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void cancelOneTask(String deviceId, Long taskId, Long userId) {
        SubTask subTask = subTaskDAO.findOne(taskId, deviceId);
        subTask.setEndTime(new Date());
        subTask.setStatus(DB.TaskStatus.CANCEL);
        subTask.setUpdateBy(userId);
        subTask.setUpdateTime(new Date());
        subTaskDAO.update(subTask);
    }

    /**
     * @Description: 更新任务的视频文档位置
     * @Param: [taskCode, deviceId, fileRelativePath]
     * @Return: void
     * @Author wen
     * @Date 2018/9/4 11:53
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateVideoPath(long taskCode, String deviceId, String videoRelativePath) {

        subTaskDAO.updateVideoPath(taskCode, deviceId, videoRelativePath);
    }
}
