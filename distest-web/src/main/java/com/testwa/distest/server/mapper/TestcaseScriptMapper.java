package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.TestcaseDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseScriptMapper extends BaseMapper<TestcaseDetail, Long> {

	List<TestcaseDetail> findByTestcaseId(TestcaseDetail app);

    int deleteByTestcaseId(Long testcaseId);
}