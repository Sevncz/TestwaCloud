package com.testwa.distest.server.web.app.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.app.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class AppInfoValidator {

    @Autowired
    private AppInfoService appInfoService;

    public AppInfo validateAppInfoExist(Long entityId) {
        AppInfo entity = appInfoService.get(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "App不存在");
        }
        return entity;
    }
}
