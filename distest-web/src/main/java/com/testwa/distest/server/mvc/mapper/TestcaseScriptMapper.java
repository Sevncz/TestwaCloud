package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.Testcase;
import com.testwa.core.entity.TestcaseScript;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestcaseScriptMapper extends BaseMapper<TestcaseScript, Long> {

	List<TestcaseScript> findByTestcaseId(TestcaseScript app);

}