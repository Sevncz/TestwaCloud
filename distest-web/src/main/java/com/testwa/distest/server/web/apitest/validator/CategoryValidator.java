package com.testwa.distest.server.web.apitest.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.service.apitest.service.ApiCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wen
 * @create 2018-12-24 13:34
 */
@Component
public class CategoryValidator {

    @Autowired
    private ApiCategoryService apiCategoryService;

    public ApiCategory validateCategoryExist(Long entityId) {
        ApiCategory entity = apiCategoryService.get(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "Api 分类不存在");
        }
        return entity;
    }

}
