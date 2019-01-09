package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueOperationLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IssueOperationLogMapper extends BaseMapper<IssueOperationLog, Long> {

    List<Long> listOperationUserId(@Param("issueId") Long issueId);
}