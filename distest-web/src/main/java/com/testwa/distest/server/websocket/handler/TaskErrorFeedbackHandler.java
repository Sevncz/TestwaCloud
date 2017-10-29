package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.common.enums.DB;
import com.testwa.core.entity.Task;
import com.testwa.distest.server.service.task.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TaskErrorFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(TaskErrorFeedbackHandler.class);

    @Autowired
    private TaskService taskService;


    @OnEvent(value = WebsocketEvent.FB_APPIUM_ERROR)
    public void onAppiumError(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        log.info("receive message for appium error");
        Task t = JSON.parseObject(data, Task.class);
        Task task = taskService.findOne(t.getId());
        if(task == null){
            log.error("This task was not found, {}", t.toString());
            return;
        }
        task.setStatus(DB.TaskStatus.ERROR);
        task.setEndTime(new Date());
        task.setErrorMsg(t.getErrorMsg());
        taskService.update(task);

    }

}