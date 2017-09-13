package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.UserDeviceHis;

import java.io.Serializable;

/**
 * Created by wen on 16/9/7.
 */
public interface UserDeviceHisRepository extends CommonRepository<UserDeviceHis, Serializable> {

    UserDeviceHis findByUserIdAndDeviceId(String userId, String deviceId);

    Integer countByUserId(String id);

}
