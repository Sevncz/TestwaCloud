package com.testwa.distest.common.mapper;

import com.testwa.core.common.bo.Entity;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
@Repository
public interface BaseMapper<T extends Entity, ID extends Serializable> {

    T findOne(Map<String, Object> params);

    List<T> find(Map<String, Object> params);

    List<T> findList(Map<String, Object> params);

    int delete(Map<String, Object> params);

    int deleteAll(Map<String, Object> params);

    long insert(Map<String, Object> params);

    int update(Map<String, Object> params);

    long count(Map<String, Object> params);

}
