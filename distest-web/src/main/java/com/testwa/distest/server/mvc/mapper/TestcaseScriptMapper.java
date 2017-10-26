package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.mvc.entity.Testcase;
import com.testwa.distest.server.mvc.entity.TestcaseScript;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestcaseScriptMapper extends BaseMapper<TestcaseScript, Long> {

	List<TestcaseScript> findByTestcaseId(TestcaseScript app);

}