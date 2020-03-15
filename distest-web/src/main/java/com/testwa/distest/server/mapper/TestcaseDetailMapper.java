package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.TestcaseDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseDetailMapper extends BaseMapper<TestcaseDetail, Long> {

    int deleteByTestcaseId(Long testcaseId);

}