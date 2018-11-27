package com.testwa.distest.server.web.wallet.mgr;


import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletMgr {

    @Autowired
    private DeviceLogService deviceLogService;

    private User user;

    @Autowired
    public WalletMgr setUser(User user) {
        this.user = user;
        return this;
    }

    public Long totalEquipmentDuration() {
        Long debugTime = deviceLogService.sumDebugTime(user);
        Long jobTime = deviceLogService.sumJobTime(user);
        return (debugTime + jobTime) / 1000;
    }

}
