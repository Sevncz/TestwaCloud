package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaDevice;
import com.testwa.distest.server.repository.TestwaDeviceRepository;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
@CacheConfig
public class TestwaDeviceService extends BaseService{
    private static final Logger log = LoggerFactory.getLogger(TestwaDeviceService.class);

    @Autowired
    private TestwaDeviceRepository testwaDeviceRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<TestwaDevice> findAll(){
        return testwaDeviceRepository.findAll();
    }

    public Page<TestwaDevice> findAll(PageRequest page){
        return testwaDeviceRepository.findAll(page);
    }

    @Cacheable(value = "cache.device", keyGenerator = "wiselyKeyGenerator")
    public TestwaDevice getDeviceById(String deviceId){
        TestwaDevice td = testwaDeviceRepository.findOne(deviceId);
        return td;
    }

    public void save(TestwaDevice testwaDevice) {
        testwaDeviceRepository.save(testwaDevice);
    }

    public Page<TestwaDevice> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return testwaDeviceRepository.find(query, pageRequest);
    }
}
