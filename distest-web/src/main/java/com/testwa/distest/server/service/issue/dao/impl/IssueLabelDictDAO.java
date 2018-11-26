package com.testwa.distest.server.service.issue.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.IssueLabelDict;
import com.testwa.distest.server.mapper.IssueLabelDictMapper;
import com.testwa.distest.server.service.issue.dao.IIssueLabelDictDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IssueLabelDictDAO extends BaseDAO<IssueLabelDict, Long> implements IIssueLabelDictDAO {
    @Autowired
    private IssueLabelDictMapper mapper;
}
