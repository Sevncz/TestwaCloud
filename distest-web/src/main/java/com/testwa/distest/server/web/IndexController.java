package com.testwa.distest.server.web;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.server.mvc.event.GameOverEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by wen on 7/30/16.
 */
@ApiIgnore
@RestController
public class IndexController extends BaseController {

    @Autowired
    ApplicationContext context;

    @RequestMapping(value = {"/", ""})
    String index() {
        return "Welcome to testwa. If you need some help, please visite http://www.testwa.com. Thanks";
    }

    @ResponseBody
    @RequestMapping(value = "/env")
    Result evn() {
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/test")
    Result test() {

        // 根据前端需求开始统计报告
        String exeId = "59a16da99d0437ab3a9448e6";

        context.publishEvent(new GameOverEvent(this, exeId));

        return ok();
    }

}
