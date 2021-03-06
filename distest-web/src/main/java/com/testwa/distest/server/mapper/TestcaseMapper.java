package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.Testcase;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseMapper extends BaseMapper<Testcase, Long> {

	List<Testcase> findBy(Testcase app);

	long countBy(Testcase query);

    List<Testcase> findAllOrder(@Param("testcaseIds") List<Long> cases, @Param("order") String order);

	List<Testcase> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    Testcase fetchOne(@Param("testcaseId") Long testcaseId);

    List<Testcase> fetchContainsScripts(@Param("scriptIds") List<Long> scriptIds);
}