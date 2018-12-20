package com.testwa.distest.server.web.apitest.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.common.validator.FileUploadValidator;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.postman.model.PostmanCollection;
import com.testwa.distest.postman.model.PostmanEnvironment;
import com.testwa.distest.server.service.apitest.service.PostmanEnvService;
import com.testwa.distest.server.service.apitest.service.PostmanService;
import com.testwa.distest.server.web.apitest.vo.PostmanCollectionVO;
import com.testwa.distest.server.web.apitest.vo.PostmanEnvironmentVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * Postman测试相关接口
 *
 * @author wen
 * @create 2018-12-04 18:41
 */
@Slf4j
@Api("Postman测试相关接口")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class PostmanController extends BaseController {
    private static final String[] allowExtName = {".json"};
    private static final long fileSize = 1024 * 1024 * 10;  // 10M

    @Autowired
    private PostmanService postmanService;
    @Autowired
    private PostmanEnvService postmanEnvService;
    @Autowired
    private FileUploadValidator fileUploadValidator;
    @Autowired
    private ProjectValidator projectValidator;

    @ApiOperation(value="新建一个postman配置")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/postmanNew")
    public Long postmanNew(@PathVariable("projectId") Long projectId, @RequestParam("collectionFile") MultipartFile collectionFile) throws Exception {
        fileUploadValidator.validateFile(collectionFile, fileSize, allowExtName);
        projectValidator.validateProjectExist(projectId);
        return postmanService.upload(projectId, collectionFile);
    }

    @ApiOperation(value="新建一个postman环境配置")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/postmanEnvNew")
    public Long postmanEnvNew(@PathVariable("projectId") Long projectId, @RequestParam("environmentFile") MultipartFile environmentFile) throws Exception {
        fileUploadValidator.validateFile(environmentFile, fileSize, allowExtName);
        projectValidator.validateProjectExist(projectId);
        return postmanEnvService.upload(projectId, environmentFile);
    }

    @ApiOperation(value="删除一个postman配置")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/{postmanId}/postmanDelete")
    public void postmanDelete(@PathVariable("projectId") Long projectId, @PathVariable("postmanId") Long postmanId) {
        projectValidator.validateProjectExist(projectId);
        postmanService.disable(postmanId);
    }

    @ApiOperation(value="删除一个postman环境配置")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/{postmanId}/postmanEnvDelete")
    public void postmanEnvDelete(@PathVariable("projectId") Long projectId, @PathVariable("postmanId") Long postmanId) {
        projectValidator.validateProjectExist(projectId);
        postmanEnvService.delete(postmanId);
    }

    @ApiOperation(value="获得解析后的collection结构")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/api/postman/{postmanId}")
    public PostmanCollectionVO getPostmanCollection(@PathVariable("projectId") Long projectId, @PathVariable("postmanId") String postmanId) {
        projectValidator.validateProjectExist(projectId);
        PostmanCollection collection = postmanService.getPostmanCollection(postmanId);
        if(collection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "对象不存在");
        }
        collection.init();
        PostmanCollectionVO vo = buildVO(collection, PostmanCollectionVO.class);
        vo.setItems(collection.getRootFolder().getItems());
        return vo;
    }

    @ApiOperation(value="获得解析后的environment结构")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/api/postmanEnv/{envId}")
    public PostmanEnvironmentVO getPostmanEnvironment(@PathVariable("projectId") Long projectId, @PathVariable("envId") String envId) {
        projectValidator.validateProjectExist(projectId);
        PostmanEnvironment environment = postmanEnvService.getPostmanEnvironment(envId);
        if(environment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "对象不存在");
        }
        PostmanEnvironmentVO vo = buildVO(environment, PostmanEnvironmentVO.class);
        return vo;
    }






}
