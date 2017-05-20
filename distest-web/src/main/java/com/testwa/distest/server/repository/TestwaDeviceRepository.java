package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaDevice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

/**
 * Created by wen on 16/9/7.
 */
public interface TestwaDeviceRepository extends CommonRepository<TestwaDevice, Serializable> {
}
