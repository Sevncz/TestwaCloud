package com.testwa.distest.server.web.auth.controller;

import com.testwa.distest.common.constant.WebConstants;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 24/10/2017.
 */
@Api("用户管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController {
}
