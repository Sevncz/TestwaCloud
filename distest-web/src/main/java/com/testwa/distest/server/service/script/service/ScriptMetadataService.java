package com.testwa.distest.server.service.script.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.ScriptMetadata;
import com.testwa.distest.server.mapper.ScriptCaseMapper;
import com.testwa.distest.server.mapper.ScriptMetadataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by wen on 09/03/2020.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptMetadataService extends BaseService<ScriptMetadata, Long> {
    @Autowired
    private ScriptMetadataMapper scriptMetadataMapper;

    public Map<String, String> getPython() {
        List<ScriptMetadata> pyMeta = scriptMetadataMapper.selectListByProperty(ScriptMetadata::getLanguage, DB.ScriptLN.PYTHON);
        Map<String, String> result = new HashMap<>();
        pyMeta.forEach(p -> {
            result.put(p.getStrategyKey(), p.getStrategyValue());
        });
        return result;
    }
}
