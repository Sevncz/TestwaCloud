package com.testwa.distest.server.web.wallet.mgr;


import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletMgr {

    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    public Long sumUseDeviceTimeByUser(String username) {
        User user = userService.findByUsername(username);
        Long debugTime = deviceLogService.sumDebugTime(user);
        Long jobTime = deviceLogService.sumJobTime(user);
        return (debugTime + jobTime) / 1000;
    }

}
