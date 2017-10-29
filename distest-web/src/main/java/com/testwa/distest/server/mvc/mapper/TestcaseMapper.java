package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.Script;
import com.testwa.core.entity.Testcase;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.Map;

public interface TestcaseMapper extends BaseMapper<Testcase, Long> {

	List<Testcase> findBy(Testcase app);

	long countBy(Testcase query);

    List<Testcase> findAllOrder(@Param("testcaseIds") List<Long> cases, @Param("order") String order);
}