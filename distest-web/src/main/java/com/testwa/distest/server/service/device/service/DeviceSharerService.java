package com.testwa.distest.server.service.device.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.service.device.dao.IDeviceSharerDAO;
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
public class DeviceSharerService {

    @Autowired
    private IDeviceSharerDAO deviceSharerDAO;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
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
        DeviceSharer oldScopeUser = deviceSharerDAO.findShareUserIn(deviceId, ownerId, toUserId);
        if(oldScopeUser == null) {
            deviceSharerDAO.insert(scopeUser);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void insertList(String deviceId, Long ownerId, Set<Long> userIds) {
        if(userIds == null || userIds.size() == 0) {
            return;
        }
        userIds.forEach( id -> {
            this.insert(deviceId, ownerId, id, DB.DeviceShareScopeTypeEnum.User);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void removeList(String deviceId, Long ownerId, Set<Long> userIds) {
        if(userIds == null || userIds.size() == 0) {
            return;
        }
        userIds.forEach( id -> {
            this.removeOne(deviceId, id, ownerId);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void removeList(List<Long> entityIds) {
        if(entityIds == null) {
            return;
        }
        Set<Long> entitySet = new HashSet<>(entityIds);
        if(entitySet.size() == 0) {
            return;
        }
        if(entitySet.size() == 1) {
            deviceSharerDAO.delete(entityIds.get(0));
        }else{
            deviceSharerDAO.delete(entitySet);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void removeOne(String deviceId, Long shareId, Long ownerId) {
        deviceSharerDAO.removeOne(deviceId, shareId, ownerId);
    }

    /**
     * @Description: 获得分享给用户的 DeviceSharer 列表
     * @Param: [inDeviceList, userId]
     * @Return: java.util.List<com.testwa.distest.server.entity.DeviceSharer>
     * @Author wen
     * @Date 2018/10/30 18:34
     */
    public List<DeviceSharer> findShareToUserList(Set<String> inDeviceList, Long userId) {

        return deviceSharerDAO.findShareToUserList(inDeviceList, userId);
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

        List<DeviceSharerDTO> dtoList = deviceSharerDAO.findDeviceScopeUserList(deviceId, userId);

        dtoList.forEach(dto -> {
            DeviceScopeUserVO vo = new DeviceScopeUserVO();
            BeanUtils.copyProperties(dto, vo);
            scopeUserVOList.add(vo);
        });

        return scopeUserVOList;
    }

}
