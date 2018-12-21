package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabelMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueLabelMapMapper extends BaseMapper<IssueLabelMap, Long> {

    void deleteByLabelId(@Param("labelId") Long labelId);

    void deleteByIssueId(@Param("issueId") Long issueId);

}