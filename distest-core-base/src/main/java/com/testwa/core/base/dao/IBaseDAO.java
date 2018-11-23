package com.testwa.core.base.dao;

import com.github.pagehelper.PageInfo;
import com.testwa.core.base.bo.Entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface IBaseDAO <T extends Entity, ID extends Serializable> {

    /**
     * 删除一个对象
     * @param key
     * @return
     */
    int delete(Serializable key);

    /**
     * 删除多个对象
     * @param keys
     * @return
     */
    int delete(Collection<ID> keys);

    /**
     * 保存对象
     * @param entity
     * @return id
     */
    long insert(T entity);

    /**
     * 更新对象
     * @param entity
     * @return 更新的行数
     */
    int update(T entity);

    /**
     * 查找所有
     * @return
     */
    List<T> findAll();

    /**
     * 根据主键集合获取结果列表
     * @param keys
     * @return
     */
    List<T> findAll(Collection<ID> keys);

    /**
     * 根据entity参数获取list
     * @param entity
     * @return
     */
    List<T> findBy(T entity);

    /**
     * 根据主键获得一个对象
     * @param id
     * @return
     */
    T findOne(Serializable id);

    /**
     * 分页结果列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<T> findPage(int pageNum, int pageSize);
    /**
     * 分页结果列表
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageInfo<T> findPage(int pageNum, int pageSize, String orderBy, String order);

    /**
     * 数量
     * @return
     */
    long count();

}