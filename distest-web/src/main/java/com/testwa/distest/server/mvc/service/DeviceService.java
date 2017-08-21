package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.TDevice;
import com.testwa.distest.server.mvc.repository.DeviceRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
@CacheConfig
public class DeviceService extends BaseService{
    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    private DeviceRepository deviceRepository;

    public List<TDevice> findAll(){
        return deviceRepository.findAll();
    }

    public Page<TDevice> findAll(PageRequest page){
        return deviceRepository.findAll(page);
    }

    @Cacheable(value = "cache.device")
    public TDevice getDeviceById(String deviceId){
        TDevice td = deviceRepository.findOne(deviceId);
        return td;
    }

    public void save(TDevice tDevice) {
        deviceRepository.save(tDevice);
    }

    public Page<TDevice> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        return null;
    }

    public List<TDevice> find(String deviceId) {

        List<Criteria> andCriteria = new ArrayList<>();
        if(StringUtils.isNotEmpty(deviceId)){
            andCriteria.add(Criteria.where("id").is(deviceId));
        }
        andCriteria.add(Criteria.where("disable").is(false));

        Query query = buildQueryByCriteria(andCriteria, null);

        return deviceRepository.find(query);
    }
}
