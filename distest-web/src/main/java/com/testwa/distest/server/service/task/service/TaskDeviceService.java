package com.testwa.distest.server.service.task.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.repository.AppiumRunningLogRepository;
import com.testwa.distest.server.service.task.dao.ITaskDeviceDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
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
public class TaskDeviceService {

    @Autowired
    private ITaskDeviceDAO taskDeviceDAO;
    @Autowired
    private AppiumRunningLogRepository procedureInfoRepository;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(TaskDevice entity) {
        return taskDeviceDAO.insert(entity);
    }

    public TaskDevice findOne(Long entityId) {
        return taskDeviceDAO.findOne(entityId);
    }

    public List<TaskDevice> findAll(List<Long> entityIds) {
        return taskDeviceDAO.findAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(TaskDevice entity) {
        taskDeviceDAO.update(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteTaskDevice(List<Long> entityIds) {
        taskDeviceDAO.delete(entityIds);
        entityIds.forEach( id -> {
            List<AppiumRunningLog> infos = procedureInfoRepository.findByTaskCodeOrderByTimestampAsc(id);
            procedureInfoRepository.delete(infos);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteTaskDevice(Long taskCode) {
        taskDeviceDAO.disableAll(taskCode);
    }

    public List<TaskDevice> getRunningtaskDevice(Long projectId, Long userId) {
        TaskDevice query = new TaskDevice();
        query.setStatus(DB.TaskStatus.RUNNING);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDeviceDAO.findBy(query);
    }

    public List<TaskDevice> getRecentFinishedRunningTask(Long projectId, Long userId) {
        TaskDevice query = new TaskDevice();
        query.setStatus(DB.TaskStatus.COMPLETE);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDeviceDAO.findBy(query);
    }

    public TaskDevice findOne(Long taskCode, String deviceId) {
        return taskDeviceDAO.findOne(taskCode, deviceId);
    }

    /**
     *@Description: 按状态顺序返回
     *@Param: [taskCode]
     *@Return: java.util.List<com.testwa.distest.server.entity.TaskDevice>
     *@Author: wen
     *@Date: 2018/5/3
     */
    public List<TaskDevice> findByTaskCode(Long taskCode) {
        return taskDeviceDAO.findByTaskCode(taskCode);
    }

    /**
     *@Description: 返回TaskDevice各个状态的数量
     *@Param: [taskCode]
     *@Return: java.util.Map<java.lang.String,java.lang.Integer>
     *@Author: wen
     *@Date: 2018/5/3
     */
    public List<TaskDeviceStatusStatis> countTaskDeviceStatus(Long taskCode) {
        return taskDeviceDAO.countTaskDeviceStatus(taskCode);
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
        TaskDevice taskDevice = taskDeviceDAO.findOne(taskId, deviceId);
        taskDevice.setEndTime(new Date());
        taskDevice.setStatus(DB.TaskStatus.CANCEL);
        taskDevice.setUpdateBy(userId);
        taskDevice.setUpdateTime(new Date());
        taskDeviceDAO.update(taskDevice);
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

        taskDeviceDAO.updateVideoPath(taskCode, deviceId, videoRelativePath);
    }
}
