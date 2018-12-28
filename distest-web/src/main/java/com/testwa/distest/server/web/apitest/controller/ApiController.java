package com.testwa.distest.server.web.apitest.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Api;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.service.apitest.form.ApiNewForm;
import com.testwa.distest.server.service.apitest.form.CategoryNewForm;
import com.testwa.distest.server.service.apitest.service.ApiCategoryService;
import com.testwa.distest.server.service.apitest.service.ApiService;
import com.testwa.distest.server.web.apitest.validator.ApiValidator;
import com.testwa.distest.server.web.apitest.validator.CategoryValidator;
import com.testwa.distest.server.web.apitest.vo.ApiCategoryVO;
import com.testwa.distest.server.web.apitest.vo.ApiDetailVO;
import com.testwa.distest.server.web.apitest.vo.ApiVO;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Api 相关接口
 *
 * @author wen
 * @create 2018-12-17 18:44
 */
@Slf4j
@io.swagger.annotations.Api("Api管理")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class ApiController {

    @Autowired
    private ApiService apiService;
    @Autowired
    private ApiCategoryService apiCategoryService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ApiValidator apiValidator;
    @Autowired
    private CategoryValidator categoryValidator;

    @ApiOperation(value="新建一个分类")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/apiCategoryNew")
    public Long saveCategory(@PathVariable("projectId") Long projectId, @RequestBody @Valid CategoryNewForm form, @RequestParam("parentId") Long parentId) {
        projectValidator.validateProjectExist(projectId);
        if(parentId != null) {
            categoryValidator.validateCategoryExist(parentId);
        }else{
            parentId = apiCategoryService.getRootParentId();
        }
        ApiCategory category = apiCategoryService.save(projectId, parentId, form);
        return category.getId();
    }

    @ApiOperation(value="分类列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/apiCategoryList")
    public List<ApiCategoryVO> categoryList(@PathVariable("projectId") Long projectId) {
        projectValidator.validateProjectExist(projectId);

        List<ApiCategory> categoryList = apiCategoryService.listByProjectId(projectId);
        List<Api> apiList = apiService.listByProjectId(projectId);
        Map<Long, List<ApiVO>> apiVOMap = apiList.stream().map( api -> {
                    ApiVO voTemp = new ApiVO();
                    BeanUtils.copyProperties(api, voTemp);
                    return voTemp;
                }).collect(Collectors.groupingBy(ApiVO::getCategoryId));
        Map<Long, List<ApiCategoryVO>> parentIdCategoryMap = new HashMap<>();
        // 构建树形结构
        if(categoryList != null && !categoryList.isEmpty()) {
            categoryList.forEach( category -> {
                Long parentId = category.getParentId();
                List<ApiCategoryVO> voTempList = parentIdCategoryMap.get(parentId);
                if(voTempList == null ) {
                    voTempList = new ArrayList<>();
                }
                ApiCategoryVO voTemp = new ApiCategoryVO();
                BeanUtils.copyProperties(category, voTemp);
                voTemp.addItems(apiVOMap.get(category.getId()));
                voTempList.add(voTemp);
                parentIdCategoryMap.put(parentId, voTempList);
            });
        }

        return buildCategoryTree(apiCategoryService.getRootParentId(), parentIdCategoryMap);
    }

    protected List<ApiCategoryVO> buildCategoryTree(Long parentCategoryId, Map<Long, List<ApiCategoryVO>> parentIdCategoryMap) {
        List<ApiCategoryVO> voList = new ArrayList();
        List<ApiCategoryVO> rootCategoryList = parentIdCategoryMap.get(parentCategoryId);
        if( rootCategoryList != null && !rootCategoryList.isEmpty()) {
            rootCategoryList.forEach( root -> {
                voList.add(root);
                buildCategoryTree2(root, parentIdCategoryMap);
            });
        }
        return voList;
    }

    protected void buildCategoryTree2(ApiCategoryVO parentCategory, Map<Long, List<ApiCategoryVO>> parentIdCategoryMap) {
        List<ApiCategoryVO> subCategoryList = parentIdCategoryMap.get(parentCategory.getId());
        if(subCategoryList != null && !subCategoryList.isEmpty()) {
            // 该分类下还有其他分类，需要将这些分类添加至该分类下
            subCategoryList.forEach( category -> {
                parentCategory.add(category);
                buildCategoryTree2(category, parentIdCategoryMap);
            });
        }
    }

    @ApiOperation(value="移动一个分类到另一个父类")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/apiCategory/{categoryId}/moveTo/{otherCategoryId}")
    public void moveToOtherCategory(@PathVariable("projectId") Long projectId, @PathVariable("categoryId") Long categoryId,  @PathVariable("otherCategoryId") Long otherCategoryId) {
        Project project = projectValidator.validateProjectExist(projectId);

        if(categoryId.equals(otherCategoryId)) {
            return;
        }

        ApiCategory category = categoryValidator.validateCategoryExist(categoryId);
        if(!category.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + category.getCategoryName() +" 不属于项目" + project.getProjectName());
        }
        if(apiCategoryService.getRootParentId().equals(category.getParentId())) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, category.getCategoryName() + "不能移动至其他分类下");
        }

        ApiCategory otherCategory = categoryValidator.validateCategoryExist(otherCategoryId);
        if(!otherCategory.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + otherCategory.getCategoryName() +" 不属于项目" + project.getProjectName());
        }
        if(otherCategory.getPath().startsWith(category.getPath())) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + category.getCategoryName() +" 无法移到其子类" + otherCategory.getCategoryName());
        }

        apiCategoryService.updateParent(categoryId, otherCategoryId);
    }

    @ApiOperation(value=" 删除一个分类")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/apiCategory/{categoryId}/delete")
    public void deleteCategory(@PathVariable("projectId") Long projectId, @PathVariable("categoryId") Long categoryId) {
        Project project = projectValidator.validateProjectExist(projectId);

        ApiCategory category = categoryValidator.validateCategoryExist(categoryId);
        if(!category.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + category.getCategoryName() +" 不属于项目" + project.getProjectName());
        }
        if(apiCategoryService.getRootParentId().equals(category.getParentId())) {
            throw new BusinessException(ResultCode.ILLEGAL_PARAM, category.getCategoryName() + "不能移动至其他分类下");
        }

        apiCategoryService.remove(categoryId);
    }

    @ApiOperation(value="新建一个Api")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/apiCategory/{categoryId}/apiNew")
    public Long save(@PathVariable("projectId") Long projectId, @PathVariable("categoryId") Long categoryId, @RequestBody @Valid ApiNewForm form) {
        Project project = projectValidator.validateProjectExist(projectId);
        ApiCategory category = categoryValidator.validateCategoryExist(categoryId);
        if(!category.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该分类不属于项目" + project.getProjectName());
        }
        Api api = apiService.save(projectId, categoryId, form);
        return api.getId();
    }

    @ApiOperation(value="获得分类下的 Api 列表")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/apiCategory/{categoryId}/apiList")
    public List<com.testwa.distest.server.entity.Api> apiList(@PathVariable("projectId") Long projectId, @PathVariable("categoryId") Long categoryId) {
        Project project = projectValidator.validateProjectExist(projectId);
        ApiCategory category = categoryValidator.validateCategoryExist(categoryId);
        if(!category.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + category.getCategoryName() + " 不属于项目" + project.getProjectName());
        }

        return apiService.listByCategoryId(categoryId);
    }

    @ApiOperation(value="移动 api 到另一个分类")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/{apiId}/moveTo/{otherCategoryId}")
    public void apiMoveToOtherCategory(@PathVariable("projectId") Long projectId,
                                         @PathVariable("apiId") Long apiId,
                                         @PathVariable("otherCategoryId") Long otherCategoryId) {
        Project project = projectValidator.validateProjectExist(projectId);
        com.testwa.distest.server.entity.Api api = apiValidator.validateApiExist(apiId);
        if(!api.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该 API 不属于项目" + project.getProjectName());
        }
        ApiCategory newCategory = categoryValidator.validateCategoryExist(otherCategoryId);
        if(!newCategory.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "分类 " + newCategory.getCategoryName() +" 不属于项目" + project.getProjectName());
        }

        apiService.updateCategory(apiId, otherCategoryId);
    }

    @ApiOperation(value="删除一个 api")
    @ResponseBody
    @PostMapping(value = "/project/{projectId}/api/{apiId}/delete")
    public void apiMoveToOtherCategory(@PathVariable("projectId") Long projectId, @PathVariable("apiId") Long apiId) {
        Project project = projectValidator.validateProjectExist(projectId);
        Api api = apiValidator.validateApiExist(apiId);
        if(!api.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该 API 不属于项目" + project.getProjectName());
        }
        apiService.disable(apiId);
    }

    @ApiOperation(value="api 详情")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/api/{apiId}")
    public ApiDetailVO apiDetail(@PathVariable("projectId") Long projectId, @PathVariable("apiId") Long apiId) {
        Project project = projectValidator.validateProjectExist(projectId);
        Api api = apiValidator.validateApiExist(apiId);
        if(!api.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ILLEGAL_OP, "该 API 不属于项目" + project.getProjectName());
        }
        ApiDetailVO vo = new ApiDetailVO();
        BeanUtils.copyProperties(api, vo);
        return vo;
    }

}
