package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Testcase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TestcaseMapper extends BaseMapper<Testcase, Long> {

	List<Testcase> findBy(Testcase app);

	long countBy(Testcase query);

    List<Testcase> findAllOrder(@Param("testcaseIds") List<Long> cases, @Param("order") String order);

    Testcase findOne(Long key);

	List<Testcase> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    List<Testcase> findByFromProject(@Param("params") Map<String, Object> params);
}