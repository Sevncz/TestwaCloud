package com.testwa.distest.server.web.app.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("应用统计相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/app/statis")
public class AppStatisController extends BaseController {
}
