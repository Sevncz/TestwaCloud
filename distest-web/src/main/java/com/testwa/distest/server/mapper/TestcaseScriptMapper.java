package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.TestcaseScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseScriptMapper extends BaseMapper<TestcaseScript, Long> {

	List<TestcaseScript> findByTestcaseId(TestcaseScript app);

    int deleteByTestcaseId(Long testcaseId);
}