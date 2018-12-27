package com.testwa.distest.server.service.apitest.service;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.ApiCondition;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ApiCategoryMapper;
import com.testwa.distest.server.mapper.ApiMapper;
import com.testwa.distest.server.service.apitest.form.ApiNewForm;
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
    private ApiCategoryMapper apiCategoryMapper;
    @Autowired
    private User currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Api save(Long projectId, Long categoryId, ApiNewForm form) {
        return save(projectId, categoryId,
                form.getApiName(),
                form.getUrl(),
                form.getMethod(),
                form.getParam(),
                form.getAuthorization(),
                form.getHeader(),
                form.getBody(),
                form.getPreScript(),
                form.getScript(), form.getDescription());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Api save(Long projectId, Long categoryId, String apiName, String url, String method, List<Map<String, String>> param, Map<String, String> authorization, List<Map<String, String>> header, Map<String, String> body, String preScript, String script, String description) {
        Api api = new Api();
        api.setProjectId(projectId);
        api.setApiName(apiName);
        ApiCategory category = apiCategoryMapper.selectById(categoryId);
        if(category == null || !category.getEnabled()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类" + categoryId + "未找到");
        }
        api.setCategoryId(categoryId);
        api.setUrl(url);
        api.setMethod(method);
        if(param != null && !param.isEmpty()){
            api.setParam(JSON.toJSONString(param));
        }
        if(authorization != null && !authorization.isEmpty()){
            api.setAuthorization(JSON.toJSONString(authorization));
        }
        if(header != null && !header.isEmpty()){
            api.setHeader(JSON.toJSONString(header));
        }
        if(body != null && !body.isEmpty()){
            api.setBody(JSON.toJSONString(body));
        }
        api.setPreScript(preScript);
        api.setScript(script);
        api.setDescription(description);

        api.setCreateBy(currentUser.getId());
        api.setCreateTime(new Date());
        api.setEnabled(true);
        apiMapper.insert(api);
        return api;
    }

    public List<Api> listByCategoryId(Long categoryId) {
        ApiCondition condition = new ApiCondition();
        condition.setCategoryId(categoryId);
        return apiMapper.selectByCondition(condition);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCategory(Long apiId, Long otherCategoryId) {
        ApiCategory category = apiCategoryMapper.selectById(otherCategoryId);
        if(category == null || !category.getEnabled()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类" + otherCategoryId + "未找到");
        }
        Api api = get(apiId);
        api.setCategoryId(otherCategoryId);
        apiMapper.update(api);
    }

    public List<Api> listByProjectId(Long projectId) {
        ApiCondition condition = new ApiCondition();
        condition.setProjectId(projectId);
        return apiMapper.selectByCondition(condition);
    }
}
