package com.testwa.distest.server.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.TestcaseScript;

import java.util.List;

public interface TestcaseScriptMapper extends BaseMapper<TestcaseScript, Long> {

	List<TestcaseScript> findByTestcaseId(TestcaseScript app);

    int deleteByTestcaseId(Long testcaseId);
}