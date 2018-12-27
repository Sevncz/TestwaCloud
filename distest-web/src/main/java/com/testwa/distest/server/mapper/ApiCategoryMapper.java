package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.ApiCategory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCategoryMapper extends BaseMapper<ApiCategory, Long> {

    void disableByCategoryPath(@Param("categoryPath") String categoryPath);

    int batchUpdatePathAndLevel(@Param("oldPath") String oldPath, @Param("newPath") String newPath, @Param("diffLevel") Integer diffLevel);
}