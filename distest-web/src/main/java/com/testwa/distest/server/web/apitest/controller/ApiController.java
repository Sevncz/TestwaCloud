package com.testwa.distest.server.web.apitest.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.service.apitest.form.ApiNewForm;
import com.testwa.distest.server.service.apitest.service.ApiService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Api 相关接口
 *
 * @author wen
 * @create 2018-12-17 18:44
 */
@Slf4j
@Api("Postman测试相关接口")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class ApiController {

    @Autowired
    private ApiService apiService;
    @Autowired
    private ProjectValidator projectValidator;

    @ApiOperation(value="新建一个Api")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/new")
    public Long save(@PathVariable("projectId") Long projectId, @RequestBody @Valid ApiNewForm form) {
        projectValidator.validateProjectExist(projectId);
        return apiService.save(projectId,
                                form.getUrl(),
                                form.getMethod(),
                                form.getParam(),
                                form.getAuthorization(),
                                form.getHeader(),
                                form.getBody(),
                                form.getPreScript(),
                                form.getScript(), form.getDescription());
    }


    @ApiOperation(value="新建一个分类")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/apiCategory/new")
    public Long saveCategory(@PathVariable("projectId") Long projectId, @RequestBody @Valid ApiNewForm form) {
        projectValidator.validateProjectExist(projectId);
        return 0L;
    }

}
