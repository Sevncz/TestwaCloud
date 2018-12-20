package com.testwa.distest.server.service.device.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.mapper.DeviceSharerMapper;
import com.testwa.distest.server.service.device.dto.DeviceSharerDTO;
import com.testwa.distest.server.web.device.vo.DeviceScopeUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DeviceSharerService extends BaseService<DeviceSharer, Long> {

    @Autowired
    private DeviceSharerMapper deviceSharerMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void insert(String deviceId, Long ownerId, Long toUserId, DB.DeviceShareScopeTypeEnum shareScopeType) {
        if(toUserId == null) {
            return;
        }
        DeviceSharer scopeUser = new DeviceSharer();
        scopeUser.setDeviceId(deviceId);
        scopeUser.setFromUserId(ownerId);
        scopeUser.setSharerId(toUserId);
        scopeUser.setCreateTime(new Date());
        scopeUser.setShareScopeType(shareScopeType);
        DeviceSharer oldScopeUser = deviceSharerMapper.findShareUserIn(deviceId, ownerId, toUserId);
        if(oldScopeUser == null) {
            deviceSharerMapper.insert(scopeUser);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertList(String deviceId, Long ownerId, Set<Long> userIds) {
        if(userIds == null || userIds.isEmpty()) {
            return;
        }
        userIds.forEach( id -> {
            this.insert(deviceId, ownerId, id, DB.DeviceShareScopeTypeEnum.User);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeList(String deviceId, Long ownerId, Set<Long> userIds) {
        if(userIds == null || userIds.isEmpty()) {
            return;
        }
        userIds.forEach( id -> {
            this.removeOne(deviceId, id, ownerId);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeList(List<Long> entityIds) {
        if(entityIds == null) {
            return;
        }
        Set<Long> entitySet = new HashSet<>(entityIds);
        if(entitySet.isEmpty()) {
            return;
        }
        entitySet.forEach( id -> {
            deviceSharerMapper.delete(id);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeOne(String deviceId, Long shareId, Long ownerId) {
        deviceSharerMapper.removeOne(deviceId, shareId, ownerId);
    }

    /**
     * @Description: 获得分享给用户的 DeviceSharer 列表
     * @Param: [inDeviceList, userId]
     * @Return: java.util.List<com.testwa.distest.server.entity.DeviceSharer>
     * @Author wen
     * @Date 2018/10/30 18:34
     */
    public List<DeviceSharer> findShareToUserList(Set<String> inDeviceList, Long userId) {

        return deviceSharerMapper.findShareToUserList(inDeviceList, userId);
    }

    /**
     * @Description: 获得该设备分配的用户列表
     * @Param: [deviceId]
     * @Return: java.util.List<com.testwa.distest.server.web.device.vo.DeviceScopeUserVO>
     * @Author wen
     * @Date 2018/10/30 18:36
     */
    public List<DeviceScopeUserVO> findDeviceScopeUserList(String deviceId, Long userId) {

        List<DeviceScopeUserVO> scopeUserVOList = new ArrayList<>();

        List<DeviceSharerDTO> dtoList = deviceSharerMapper.findDeviceScopeUserList(deviceId, userId);

        dtoList.forEach(dto -> {
            DeviceScopeUserVO vo = new DeviceScopeUserVO();
            BeanUtils.copyProperties(dto, vo);
            scopeUserVOList.add(vo);
        });

        return scopeUserVOList;
    }

}
