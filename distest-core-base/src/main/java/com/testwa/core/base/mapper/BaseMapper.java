package com.testwa.core.base.mapper;

import com.testwa.core.base.bo.Entity;
import com.testwa.core.base.mybatis.SqlProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
@Mapper
public interface BaseMapper<T extends Entity, ID extends Serializable> {

    @InsertProvider(type = SqlProvider.class, method = "insert")
    @Options(useGeneratedKeys=true, keyProperty = "id")
    long insert(Map<String, Object> params);

    @UpdateProvider(type = SqlProvider.class, method = "update")
    int update(Map<String, Object> params);

    @DeleteProvider(type = SqlProvider.class, method = "delete")
    int delete(Map<String, Object> params);

    @DeleteProvider(type = SqlProvider.class, method = "deleteAll")
    int delete_all(Map<String, Object> params);

    @SelectProvider(type = SqlProvider.class, method = "findOne")
    T find_one(Map<String, Object> params);

    @SelectProvider(type = SqlProvider.class, method = "findList")
    List<T> find_list(Map<String, Object> params);

    @SelectProvider(type = SqlProvider.class, method = "count")
    long count(Map<String, Object> params);

}
