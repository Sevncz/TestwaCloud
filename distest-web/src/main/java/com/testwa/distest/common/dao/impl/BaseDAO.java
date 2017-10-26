package com.testwa.distest.common.dao.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.Entity;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.common.util.ClassUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Repository
public class BaseDAO<T extends Entity,ID extends Serializable> implements IBaseDAO<T, ID> {

    @Resource
    private BaseMapper baseMapper;

    private Class<T> entityClass = null;

    private String entitySimpleName = "";

    private String tableName = "";


    public BaseDAO() {
        entityClass = getObjectClass();
        TableName tablename =  entityClass.getAnnotation(TableName.class);
        if(tablename!=null) {
            tableName = tablename.value();
        }
        entitySimpleName = entityClass.getSimpleName();
    }

    private Class<T> getObjectClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            Type[] types = pt.getActualTypeArguments();
            return (Class<T>)types[0];
        }
        return (Class<T>) type;
    }

    @Override
    public int delete(Serializable key) {

        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("id", key);
        return baseMapper.delete(map);
    }

    @Override
    public int delete(Collection<ID> keys) {

        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("keys", keys);
        return baseMapper.deleteAll(map);
    }

    @Override
    public long insert(T entity) {

        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        Set<Field> fields = ClassUtils.getAllFiled(entityClass);
        List<String> fieldList = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();
        fields.forEach(f -> {
            try {
                f.setAccessible(true);
                if(f.get(entity) != null){
                    fieldList.add(f.getName());
                    valueList.add(f.get(entity));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        map.put("fields", fieldList);
        map.put("values", valueList);
        return baseMapper.insert(map);
    }

    @Override
    public int update(T entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        Set<Field> fields = ClassUtils.getAllFiled(entityClass);
        Map<String, Object> subMap = new HashMap<>();
        fields.forEach(f -> {
            try {
                f.setAccessible(true);
                if(f.get(entity) != null){
                    if(f.getType().isEnum() && f.getType().equals(DB.Sex.class)){
                        subMap.put(f.getName(), ((DB.Sex) f.get(entity)).getValue());
                    }else{
                        subMap.put(f.getName(), f.get(entity));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        map.put("update", subMap);
        map.put("id", entity.getId());
        return baseMapper.update(map);
    }

    @Override
    public List<T> findAll() {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        List<T> list =  baseMapper.findList(map);
        return list;
    }

    @Override
    public List<T> findAll(Collection<ID> keys) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("keys", keys);
        List<T> list =  baseMapper.findList(map);
        return list;
    }

    @Override
    public T findOne(Serializable id) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("key", id);
        T entity =  (T) baseMapper.findOne(map);
        return entity;
    }

    @Override
    public PageInfo<T> findPage(int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        PageHelper.startPage(pageNum, pageSize);
        List<T> list =  baseMapper.findList(map);
        PageInfo<T> pageInfo = new PageInfo<T>(list);
        return pageInfo;
    }

    @Override
    public PageInfo<T> findPage(int pageNum, int pageSize, String orderBy, String order) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy(orderBy + " " + order );
        List<T> list =  baseMapper.findList(map);
        PageInfo<T> pageInfo = new PageInfo<T>(list);
        return pageInfo;
    }

    @Override
    public long count() {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        return baseMapper.count(map);
    }
}