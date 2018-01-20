package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAppiumFileDAO extends IBaseDAO<AppiumFile, Long> {
    /**
     * 根据taskId获得所有appiumFile
     * @param taskId
     * @return
     */
    List<AppiumFile> findAll(Long taskId);
    /**
     * 根据taskId, deviceId获得一个appiumFile
     * @param taskId
     * @return
     */
    AppiumFile findOne(Long taskId, String deviceId);

    /**
     * 删除一个task下的所有appiumFile
     * @param taskId
     * @return
     */
    int removeFromTask(Long taskId);
}
