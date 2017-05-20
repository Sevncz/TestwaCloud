package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaApp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaAppRepository extends CommonRepository<TestwaApp, Serializable> {

    List<TestwaApp> findByUserId(String userId);

}
