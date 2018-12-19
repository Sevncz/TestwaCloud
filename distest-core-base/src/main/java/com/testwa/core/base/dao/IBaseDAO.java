package com.testwa.core.base.dao;

import com.testwa.core.base.bo.Entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface IBaseDAO <T extends Entity, ID extends Serializable> {


    /**
     * 保存对象
     * @param entity
     * @return id
     */
    <T, ID> ID insert(T entity);

    /**
     * 删除一个对象
     * @param key
     * @return
     */
    <T> int deleteByIds(Serializable key);

    /**
     * 逻辑删除一个对象
     * @param key
     * @return
     */
    <T> int deleteLogic(Serializable key);

    /**
     * 删除多个对象
     * @param keys
     * @return
     */
    <T> int deleteByIds(Collection<ID> keys);

    /**
     * 更新对象
     * @param entity
     * @return 更新的行数
     */
    <T> int update(T entity);

    /**
     * 根据实体 Id 更新一条记录
     *
     * @param t            实体
     * @param isIgnoreNull 是否忽略 null 值
     * @param <T>          实体对象类型
     * @return 受影响记录数
     */
    <T> int update(T t, boolean isIgnoreNull);

    /**
     * 根据 Id 查询一条记录
     *
     * @param id    主键
     * @param <T>   实体对象类型
     * @return 实体对象
     */
    <T> T get(Serializable id);

    /**
     * 查找所有
     * @return
     */
    <T> List<T> findAll();

    /**
     * 根据主键集合获取结果列表
     * @param keys
     * @return
     */
    <T> List<T> findByIds(Collection<ID> keys);

    /**
     * 根据entity参数获取list
     * @param entity
     * @return
     */
    <T> List<T> findByProperty(T entity);

    /**
     * 数量
     * @return
     */
    long count();


}