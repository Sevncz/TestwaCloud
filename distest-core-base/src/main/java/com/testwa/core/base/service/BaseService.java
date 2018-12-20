package com.testwa.core.base.service;

import com.testwa.core.base.bo.BaseCondition;
import com.testwa.core.base.bo.Entity;
import com.testwa.core.base.mybatis.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

/**
 * @author wen
 * @create 2018-12-20 15:46
 */
public class BaseService<T extends Entity, ID extends Serializable> {
    @Autowired
    private BaseMapper<T, ID> baseMapper;

    public long insert(T entity) {
        baseMapper.insert(entity);
        return (long) entity.getId();
    }

    /**
     * @Description: 物理删除
     * @Param: [id]
     * @Return: long 受影响的行数
     * @Author wen
     * @Date 2018/12/20 16:28
     */
    public int delete(Long id) {
        return baseMapper.delete(id);
    }

    /**
     * @Description: 禁用
     * @Param: [id]
     * @Return: long 受影响的行数
     * @Author wen
     * @Date 2018/12/20 16:29
     */
    public int disable(Long id) {
        return baseMapper.updateProperty(T::getEnabled, false, id);
    }

    public int update(T entity) {
        return baseMapper.update(entity);
    }

    /**
     * @Description: 根据 ID 获得可用的对象
     * @Param: [id]
     * @Return: T
     * @Author wen
     * @Date 2018/12/20 16:37
     */
    public T get(Long id) {
        T entity = baseMapper.selectById(id);
        if(entity != null) {
            return entity.getEnabled() ? entity : null;
        }
        return null;
    }

    /**
     * @Description: 获得可用的对象列表
     * @Param: [id]
     * @Return: T
     * @Author wen
     * @Date 2018/12/20 16:37
     */
    public List<T> list() {
        return baseMapper.selectListByProperty(T::getEnabled,true);
    }

    public long count() {
        BaseCondition condition = new BaseCondition();
        condition.setEnabled(true);
        return baseMapper.count(condition);
    }

}
