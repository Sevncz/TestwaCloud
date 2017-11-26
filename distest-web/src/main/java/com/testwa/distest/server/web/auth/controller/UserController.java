package com.testwa.distest.server.web.auth.controller;

import com.testwa.core.base.constant.WebConstants;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Api("用户管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/user")
public class UserController {
}
