package com.testwa.distest.server.service.apitest.service;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.condition.ApiCondition;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ApiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wen
 * @create 2018-12-17 17:47
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ApiService extends BaseService<Api, Long> {

    @Autowired
    private ApiMapper apiMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRED)
    public long save(Long projectId, String url, String method, List<Map<String, String>> param, Map<String, String> authorization, List<Map<String, String>> header, Map<String, String> body, String preScript, String script, String description) {
        Api api = new Api();
        api.setProjectId(projectId);
        api.setUrl(url);
        api.setMethod(method);
        if(!param.isEmpty()){
            api.setParam(JSON.toJSONString(param));
        }
        if(!authorization.isEmpty()){
            api.setAuthorization(JSON.toJSONString(authorization));
        }
        if(!header.isEmpty()){
            api.setHeader(JSON.toJSONString(header));
        }
        if(!body.isEmpty()){
            api.setBody(JSON.toJSONString(body));
        }
        api.setPreScript(preScript);
        api.setScript(script);
        api.setDescription(description);

        api.setCreateBy(currentUser.getId());
        api.setCreateTime(new Date());
        api.setEnabled(true);

        apiMapper.insert(api);
        return api.getId();
    }

    public List<Api> listByCategoryId(Long categoryId) {
        ApiCondition condition = new ApiCondition();
        condition.setCategoryId(categoryId);
        return apiMapper.selectByCondition(condition);
    }
}
