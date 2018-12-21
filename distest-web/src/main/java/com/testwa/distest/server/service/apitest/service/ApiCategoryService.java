package com.testwa.distest.server.service.apitest.service;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.mapper.ApiCategoryMapper;
import com.testwa.distest.server.service.apitest.form.CategoryNewForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wen
 * @create 2018-12-21 18:03
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ApiCategoryService extends BaseService<ApiCategory, Long>  {

    @Autowired
    private ApiCategoryMapper apiCategoryMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiCategory save(Long projectId, Long parentId, CategoryNewForm form) {
        ApiCategory category = new ApiCategory();
        category.setCategoryName(form.getName());
        if(!form.getAuthorization().isEmpty()){
            category.setAuthorization(JSON.toJSONString(form.getAuthorization()));
        }

        category.setPreScript(form.getPreScript());
        category.setScript(form.getScript());
        category.setDescription(form.getDescription());

        category.setProjectId(projectId);
        if(parentId == null) {
            category.setParentId(0L);
        }else{
            category.setParentId(parentId);

        }
        apiCategoryMapper.insert(category);
        return category;
    }

}
