package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.model.TestwaScript;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaScriptRepository extends CommonRepository<TestwaScript, Serializable> {


}
