package com.testwa.distest.server.service.apitest.service;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.ApiCategoryCondition;
import com.testwa.distest.server.condition.ApiCondition;
import com.testwa.distest.server.entity.ApiCategory;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ApiCategoryMapper;
import com.testwa.distest.server.mapper.ApiMapper;
import com.testwa.distest.server.service.apitest.form.CategoryNewForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-21 18:03
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ApiCategoryService extends BaseService<ApiCategory, Long>  {
    private static final String CATEGORY_PATH_SPLIT = ",";
    private static final Long ROOT_CATEGORY_ID = 0L;

    @Autowired
    private ApiCategoryMapper apiCategoryMapper;
    @Autowired
    private ApiMapper apiMapper;
    @Autowired
    private User currentUser;

    public Long getRootParentId() {
        return ROOT_CATEGORY_ID;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiCategory save(Long projectId, Long parentId, CategoryNewForm form) {
        ApiCategory category = new ApiCategory();
        if(form.getAuthorization() !=null && !form.getAuthorization().isEmpty()){
            category.setAuthorization(JSON.toJSONString(form.getAuthorization()));
        }
        category.setPreScript(form.getPreScript());
        category.setScript(form.getScript());
        category.setDescription(form.getDescription());
        category.setCategoryName(form.getName());

        category.setProjectId(projectId);
        category.setParentId(parentId);
        apiCategoryMapper.insert(category);

        if(!ROOT_CATEGORY_ID.equals(parentId)) {
            ApiCategory parentCategory = get(parentId);
            if(parentCategory == null) {
                throw new BusinessException(ResultCode.ILLEGAL_PARAM, "错误的父分类");
            }
            // 为子分类生成节点路径和深度
            String parentPath = parentCategory.getPath();
            Integer parentLevel = parentCategory.getLevel();
            category.setPath(parentPath + CATEGORY_PATH_SPLIT + category.getId());
            category.setLevel(parentLevel + 1);
        }else{
            category.setPath(String.valueOf(category.getId()));
            category.setLevel(1);
        }
        // 更新分类的节点路径和深度
        apiCategoryMapper.update(category);
        return category;
    }

    /**
     * @Description: 编辑，公共脚本，name，描述等字段
     * @Param: [categoryId, form]
     * @Return: void
     * @Author wen
     * @Date 2018/12/24 11:55
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void edit(Long categoryId, CategoryNewForm form) {
        ApiCategory category = get(categoryId);
        if(category == null) {
            return;
        }
        if(form.getAuthorization() !=null && !form.getAuthorization().isEmpty()){
            category.setAuthorization(JSON.toJSONString(form.getAuthorization()));
        }
        category.setPreScript(form.getPreScript());
        category.setScript(form.getScript());
        category.setDescription(form.getDescription());
        category.setCategoryName(form.getName());

//        category.setUpdateBy(currentUser.getId());
//        category.setUpdateTime(new Date());

        apiCategoryMapper.update(category);

    }

    /**
     * @Description: 将该分类移动至其他分类下
     * @Param: [categoryId, newParentId]
     * @Return: void
     * @Author wen
     * @Date 2018/12/24 14:34
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateParent(Long categoryId, Long otherParentId) {
        ApiCategory category = get(categoryId);
        if(category == null) {
            return;
        }
        String oldPath = category.getPath();
        Integer oldLevel = category.getLevel();
        if(ROOT_CATEGORY_ID.equals(otherParentId)) {
            category.setLevel(0);
            category.setPath(String.valueOf(category.getId()));
            apiCategoryMapper.batchUpdatePathAndLevel(oldPath, String.valueOf(category.getId()), 0 - oldLevel);
        }else{
            ApiCategory parentCategory = get(otherParentId);
            if(parentCategory == null) {
                throw new BusinessException(ResultCode.ILLEGAL_PARAM, "错误的父分类");
            }
            String newPath = parentCategory.getPath() + CATEGORY_PATH_SPLIT + category.getId();
            Integer newLevel = parentCategory.getLevel() + 1;
            Integer diffLevel = newLevel - oldLevel;
            int line = apiCategoryMapper.batchUpdatePathAndLevel(oldPath, newPath, diffLevel);
            log.info("移动分类，受影响的行数 {}", line);
            category.setParentId(otherParentId);
            category.setLevel(parentCategory.getLevel() + 1);
            category.setPath(newPath);
        }

        apiCategoryMapper.update(category);
    }

    /**
     * @Description: 将该分类删除，并删除该分类下所有的 api
     * @Param: [categoryId]
     * @Return: void
     * @Author wen
     * @Date 2018/12/24 14:34
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(Long categoryId) {
        ApiCategory apiCategory = get(categoryId);
        if( apiCategory == null ){
            throw new BusinessException(ResultCode.NOT_FOUND, "分类" + categoryId + "未找到");
        }
        ApiCategoryCondition condition = new ApiCategoryCondition();
        condition.setPath(apiCategory.getPath() + "%");
        List<ApiCategory> apiCategoryList = apiCategoryMapper.selectByCondition(condition);
        // 禁止该分类下的所有的 category 和 api
        apiCategoryMapper.disableByCategoryPath(apiCategory.getPath());
        apiCategoryList.forEach( tmp -> {
            apiMapper.disableByCategoryId(tmp.getId());
        });
    }

    /**
     * @Description: 获得项目下的所有 Category 列表，树形结构
     * @Param: [projectId]
     * @Return: java.util.List<com.testwa.distest.server.entity.ApiCategory>
     * @Author wen
     * @Date 2018/12/24 15:22
     */
    public List<ApiCategory> listByProjectId(Long projectId) {
        ApiCategoryCondition condition = new ApiCategoryCondition();
        condition.setProjectId(projectId);
        return apiCategoryMapper.selectByCondition(condition);
    }
}
