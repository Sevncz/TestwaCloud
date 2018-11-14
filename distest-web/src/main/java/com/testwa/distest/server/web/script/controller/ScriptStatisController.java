package com.testwa.distest.server.web.script.controller;

import com.testwa.core.base.constant.WebConstants;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 21/10/2017.
 */
@Slf4j
@Api("脚本统计相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/script/statis")
public class ScriptStatisController {
}
