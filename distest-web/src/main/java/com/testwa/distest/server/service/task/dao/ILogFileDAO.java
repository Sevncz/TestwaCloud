package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.LogFile;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ILogFileDAO extends IBaseDAO<LogFile, Long> {
    /**
     * 根据taskId获得所有LoggerFile
     * @param taskId
     * @return
     */
    List<LogFile> findAll(Long taskId);
    /**
     * 根据taskId, deviceId获得一个LoggerFile
     * @param taskId
     * @return
     */
    LogFile findOne(Long taskId, String deviceId);

    /**
     * 删除一个task下的所有LoggerFile
     * @param taskId
     * @return
     */
    int removeFromTask(Long taskId);
}
