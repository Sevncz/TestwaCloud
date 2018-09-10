package com.testwa.distest.server.service.task.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.repository.AppiumRunningLogRepository;
import com.testwa.distest.server.service.task.dao.ISubTaskDAO;
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
public class SubTaskService {

    @Autowired
    private ISubTaskDAO taskDeviceDAO;
    @Autowired
    private AppiumRunningLogRepository procedureInfoRepository;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(SubTask entity) {
        return taskDeviceDAO.insert(entity);
    }

    public SubTask findOne(Long entityId) {
        return taskDeviceDAO.findOne(entityId);
    }

    public List<SubTask> findAll(List<Long> entityIds) {
        return taskDeviceDAO.findAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(SubTask entity) {
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

    public List<SubTask> getRunningtaskDevice(Long projectId, Long userId) {
        SubTask query = new SubTask();
        query.setStatus(DB.TaskStatus.RUNNING);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDeviceDAO.findBy(query);
    }

    public List<SubTask> getRecentFinishedRunningTask(Long projectId, Long userId) {
        SubTask query = new SubTask();
        query.setStatus(DB.TaskStatus.COMPLETE);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDeviceDAO.findBy(query);
    }

    public SubTask findOne(Long taskCode, String deviceId) {
        return taskDeviceDAO.findOne(taskCode, deviceId);
    }

    /**
     *@Description: 按状态顺序返回
     *@Param: [taskCode]
     *@Return: java.util.List<com.testwa.distest.server.entity.SubTask>
     *@Author: wen
     *@Date: 2018/5/3
     */
    public List<SubTask> findByTaskCode(Long taskCode) {
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
        SubTask subTask = taskDeviceDAO.findOne(taskId, deviceId);
        subTask.setEndTime(new Date());
        subTask.setStatus(DB.TaskStatus.CANCEL);
        subTask.setUpdateBy(userId);
        subTask.setUpdateTime(new Date());
        taskDeviceDAO.update(subTask);
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
