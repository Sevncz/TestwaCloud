package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaScript;
import com.testwa.distest.server.model.TestwaTestcase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaTestcaseRepository extends CommonRepository<TestwaTestcase, Serializable> {


}
