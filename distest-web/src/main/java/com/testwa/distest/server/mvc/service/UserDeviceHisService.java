package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import com.testwa.distest.server.mvc.beans.QueryFilters;
import com.testwa.distest.server.mvc.model.UserShareScope;
import com.testwa.distest.server.mvc.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public UserDeviceHis findByUserIdAndDeviceId(String userId, String deviceId) {
        return userDeviceHisRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    public Page<UserDeviceHis> findOwnerUserPage(PageRequest pageRequest, String userId, String deviceId) {

        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(deviceId)){
            andCriteria.add(Criteria.where("deviceId").regex(deviceId));
        }
        if(StringUtils.isNotEmpty(userId)){
            andCriteria.add(Criteria.where("userId").is(userId));
        }
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);
        return userDeviceHisRepository.find(query, pageRequest);
    }

    public Page<UserDeviceHis> findSharedUserPage(PageRequest pageRequest, String userId, String deviceId) {

        List<Criteria> andCriteria = new ArrayList<>();
        List<Criteria> orCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(deviceId)){
            andCriteria.add(Criteria.where("deviceId").regex(deviceId));
        }
        if(StringUtils.isNotEmpty(userId)){
            orCriteria.add(Criteria.where("shareUsers").is(userId));
        }
        orCriteria.add(Criteria.where("scope").is(UserShareScope.All.getValue()));

        andCriteria.add(Criteria.where("disable").is(false));
        Query query = buildQueryByCriteria(andCriteria, orCriteria);
        return userDeviceHisRepository.find(query, pageRequest);

    }

    public Page<UserDeviceHis> findAvailablePage(PageRequest pageRequest, String userId, String deviceId) {

        List<Criteria> andCriteria = new ArrayList<>();
        List<Criteria> orCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(deviceId)){
            andCriteria.add(Criteria.where("deviceId").regex(deviceId));
        }
        if(StringUtils.isNotEmpty(userId)){
            orCriteria.add(Criteria.where("shareUsers").in(userId));
        }
        if(StringUtils.isNotEmpty(userId)){
            orCriteria.add(Criteria.where("userId").is(userId));
        }
        orCriteria.add(Criteria.where("scope").is(UserShareScope.All.getValue()));

        andCriteria.add(Criteria.where("disable").is(false));
        Query query = buildQueryByCriteria(andCriteria, orCriteria);
        return userDeviceHisRepository.find(query, pageRequest);

    }

    public Integer getDeviceCountByUser(User user) {
        return userDeviceHisRepository.countByUserId(user.getId());
    }
}
