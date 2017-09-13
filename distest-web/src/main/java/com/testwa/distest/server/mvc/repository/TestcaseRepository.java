package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Testcase;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TestcaseRepository extends CommonRepository<Testcase, Serializable> {

    List<Testcase> findByProjectId(String projectId);

    Integer countByProjectId(String projectId);
}
