package com.testwa.distest.server.web.apitest.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.service.apitest.service.ApiCategoryService;
import com.testwa.distest.server.service.apitest.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wen
 * @create 2018-12-24 13:34
 */
@Component
public class ApiValidator {

    @Autowired
    private ApiService apiService;

    public Api validateApiExist(Long entityId) {
        Api entity = apiService.get(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "Api 不存在");
        }
        return entity;
    }

}
