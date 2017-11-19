package com.testwa.distest.server.web.script.controller;

import com.testwa.distest.common.constant.WebConstants;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wen on 21/10/2017.
 */
@Log4j2
@Api("脚本统计相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/script/statis")
public class ScriptStatisController {
}
