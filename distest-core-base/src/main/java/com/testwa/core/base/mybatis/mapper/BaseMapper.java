package com.testwa.core.base.mybatis.mapper;

import com.testwa.core.base.bo.Entity;
import com.testwa.core.base.mybatis.annotation.VersionLocker;
import com.testwa.core.base.mybatis.builder.PropertyFunction;
import org.apache.ibatis.annotations.*;

import java.io.Serializable;
import java.util.List;

@Mapper
public interface BaseMapper<T extends Entity, ID extends Serializable> {


    /**
     * 新增一条记录
     *
     * @param entity 实体
     * @return 受影响记录
     */
    @InsertProvider(type = BaseSqlProvider.class, method = "insert")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    long insert(T entity);

    /**
     * 更新一条记录
     *
     * @param entity entity
     * @return 受影响记录
     */
    @UpdateProvider(type = BaseSqlProvider.class, method = "update")
    int update(T entity);


    /**
     *  更新一个属性
     *
     * @param function
     * @param value
     * @param id
     * @param <T>
     * @param <R>
     * @return 受影响记录
     */
    @UpdateProvider(type = BaseSqlProvider.class, method = "updateProperty")
    <T, R> int updateProperty(@Param("property") PropertyFunction<T, R> function, @Param("value") Object value, @Param("id") Long id);

    /**
     * 删除一条记录
     *
     * @param id id
     * @return 受影响记录
     */
    @DeleteProvider(type = BaseSqlProvider.class, method = "delete")
    int delete(Long id);

    /**
     * 根据id查询
     *
     * @param id id
     * @return Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectById")
    T selectById(Long id);

    /**
     * 根据属性查询一条记录
     *
     * @param function property
     * @param value    value
     * @param <R>      R
     * @return Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectByProperty")
    <R> T selectByProperty(@Param("property") PropertyFunction<T, R> function, @Param("value") Object value);

    /**
     * 根据属性查询记录列表
     *
     * @param function property
     * @param value    value
     * @param <R>      R
     * @return Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectByProperty")
    <R> List<T> selectListByProperty(@Param("property") PropertyFunction<T, R> function, @Param("value") Object value);

    /**
     * 根据查询条件查询记录
     *
     * @param condition   condition
     * @param <Condition> Condition
     * @return List Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectByCondition")
    <Condition> List<T> selectByCondition(Condition condition);

    /**
     * 获得所有记录列表
     *
     * @return List Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "list")
    List<T> list();

    /**
     * 获得记录数量
     *
     * @return List Entity
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "count")
    <Condition> long count(Condition query);
}
