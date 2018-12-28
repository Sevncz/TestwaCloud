package com.testwa.distest.server.web.apitest.mgr;

import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.service.apitest.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * API send 管理器
 *
 * @author wen
 * @create 2018-12-27 17:33
 */
@Component
public class ApiSendMgr {

    @Autowired
    private ApiService apiService;

    public void send(Long apiId) {
        Api api = apiService.get(apiId);
    }

}
