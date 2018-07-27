package com.testwa.distest.server.web.device.mgr;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;import com.testwa.distest.server.service.device.service.DeviceService;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;/** * @Program: distest * @Description: 设备锁管理 * @Author: wen * @Create: 2018-07-18 10:44 **/@Slf4j@Componentpublic class DeviceLockMgr {    private final static Integer default_expire_second = 30; // 默认30分钟锁定时间    @Autowired    private DeviceLockCache cache;    @Autowired    private DeviceService deviceService;    /**     *@Description: 调试时锁定设备     *@Param: [deviceId, userCode]     *@Return: void     *@Author: wen     *@Date: 2018/7/18     */    public void debugLock(String deviceId, String userCode) {        cache.lock(deviceId, userCode, default_expire_second);        deviceService.debugging(deviceId);    }    /**     *@Description: 调试结束之后解锁     *@Param: [deviceId, userCode]     *@Return: void     *@Author: wen     *@Date: 2018/7/18     */    public void debugRelease(String deviceId) {        cache.releaseForce(deviceId);        deviceService.debugFree(deviceId);    }    /**     *@Description: 执行测试任务时锁定设备     *@Param: [deviceId, userCode]     *@Return: void     *@Author: wen     *@Date: 2018/7/18     */    public void workLock(String deviceId, String userCode) {        cache.lock(deviceId, userCode, default_expire_second);        deviceService.work(deviceId);    }    /**     *@Description: 执行测试任务时锁定设备     *@Param: [deviceId, userCode, workExpireTime]     *@Return: void     *@Author: wen     *@Date: 2018/7/18     */    public void workLock(String deviceId, String userCode, Integer workExpireTime) {        cache.lock(deviceId, userCode, workExpireTime);        deviceService.work(deviceId);    }    /**     *@Description: 任务执行完成之后解锁     *@Param: [deviceId, userCode, workExpireTime]     *@Return: void     *@Author: wen     *@Date: 2018/7/18     */    public void workRelease(String deviceId) {        cache.releaseForce(deviceId);        deviceService.release(deviceId);    }}