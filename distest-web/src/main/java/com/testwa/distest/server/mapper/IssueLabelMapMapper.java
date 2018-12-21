package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IssueLabelMap;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueLabelMapMapper extends BaseMapper<IssueLabelMap, Long> {

    void deleteByLabelId(Long labelId);
}