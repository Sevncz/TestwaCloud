package com.testwa.distest.server.service;

import com.testwa.distest.server.android.TestwaAndroidApp;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.UserDeviceHis;
import com.testwa.distest.server.model.params.QueryFilters;
import com.testwa.distest.server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class UserDeviceHisService extends BaseService {

    @Autowired
    private UserDeviceHisRepository userDeviceHisRepository;

    public void save(UserDeviceHis his){
        userDeviceHisRepository.save(his);
    }

    public void deleteById(String id){
        disableById(id, userDeviceHisRepository);
    }

    public void update(UserDeviceHis his) {
        userDeviceHisRepository.save(his);
    }

    public UserDeviceHis getById(String id){
        return userDeviceHisRepository.findOne(id);
    }

    public Page<UserDeviceHis> findAll(PageRequest pageRequest) {
        return userDeviceHisRepository.findAll(pageRequest);
    }

    public List<UserDeviceHis> findAll() {
        return userDeviceHisRepository.findAll();
    }

    public Page<UserDeviceHis> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return userDeviceHisRepository.find(query, pageRequest);
    }

    public Page<UserDeviceHis> find(QueryFilters filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return userDeviceHisRepository.find(query, pageRequest);
    }

    public List<UserDeviceHis> find(List<Map<String, String>> filters){
        Query query = buildQuery(filters);
        return userDeviceHisRepository.find(query);
    }

    public UserDeviceHis findByUserIdAndDeviceId(String userId, String deviceId) {
        return userDeviceHisRepository.findByUserIdAndDeviceId(userId, deviceId);
    }
}
