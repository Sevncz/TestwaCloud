package com.testwa.distest.server.service.issue.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.IssueLabelDict;
import com.testwa.distest.server.mapper.IssueLabelDictMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author wen
 * @create 2018-12-17 10:23
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class LabelDictService extends BaseService<IssueLabelDict, Long> {

    @Autowired
    private IssueLabelDictMapper issueLabelDictMapper;

    public long save(String name, String color) {
        IssueLabelDict issueLabelDict = new IssueLabelDict();
        issueLabelDict.setName(name);
        issueLabelDict.setColor(color);
        return issueLabelDictMapper.insert(issueLabelDict);
    }

}
