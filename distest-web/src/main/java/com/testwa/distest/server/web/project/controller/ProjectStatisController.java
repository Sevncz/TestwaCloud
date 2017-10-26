package com.testwa.distest.server.web.project.controller;

import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 20/10/2017.
 */
@Api("项目统计相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project/statis")
public class ProjectStatisController extends BaseController {
}
