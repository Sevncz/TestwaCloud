package com.testwa.distest.server.web.job.controller;


import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.quartz.JobInfoVO;
import com.testwa.distest.quartz.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api("Quartz任务相关api")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/job")
public class JobController extends BaseController {

    @Autowired
    private JobService jobService;

    @ApiOperation(value="job分页列表", notes = "")
    @ResponseBody
    @GetMapping(value = "/page")
    public PageResult page() {
        return jobService.list(0, 100);
    }

}
