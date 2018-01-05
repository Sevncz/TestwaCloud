package com.testwa.core.base.dao.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.Entity;
import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.core.base.enums.ValueEnum;
import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.core.base.util.ClassUtils;
import com.testwa.core.base.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Repository
public class BaseDAO<T extends Entity,ID extends Serializable> implements IBaseDAO<T, ID> {
    private static final Logger log = LoggerFactory.getLogger(BaseDAO.class);

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
        map.put("key", key);
        return baseMapper.delete(map);
    }

    @Override
    public int delete(Collection<ID> keys) {

        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("keys", keys);
        return baseMapper.delete_all(map);
    }

    @Override
    public long insert(T entity) {

        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        Set<Field> fields = ClassUtils.getAllFiled(entityClass);
//        List<String> fieldList = new ArrayList<>();
//        List<Object> valueList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        fields.forEach(f -> {
            try {
                f.setAccessible(true);
                if(f.get(entity) != null){
//                    fieldList.add(f.getName());
                    if(f.getType().isEnum()){
                        if(ValueEnum.class.isAssignableFrom(f.getType())){
                            ValueEnum ve = ReflectUtil.resolveValueEnum(f.get(entity).toString(), (Class<ValueEnum>)f.getType());
                            if(ve != null){
                                data.put(f.getName(), ve.getValue());
//                                valueList.add(ve.getValue());
                            }else{
                                log.error("this value not in enum");
                            }
                        }else{
                            log.error("this enum not match ValueEnum");
                        }
                    }else{
                        Column column = f.getAnnotation(Column.class);
                        if(column == null || !column.ignore()){
//                            valueList.add(f.get(entity));
                            data.put(f.getName(), f.get(entity));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
//        map.put("fields", fieldList);
//        map.put("values", valueList);
        map.put("data", data);
        long num_of_record_inserted = baseMapper.insert(map);
        return (long) map.get("id");
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
                    if(!"id".equals(f.getName())) {
                        if(f.getType().isEnum()){
                            if(ValueEnum.class.isAssignableFrom(f.getType())){
                                ValueEnum ve = ReflectUtil.resolveValueEnum(f.get(entity).toString(), (Class<ValueEnum>)f.getType());
                                if(ve != null){
                                    subMap.put(f.getName(), ve.getValue());
                                }else{
                                    log.error("this value not in enum");
                                }
                            }else{

                                log.error("this enum not match ValueEnum");
                            }
                        }else{
                            Column column = f.getAnnotation(Column.class);
                            if(column == null || !column.ignore()){
                                subMap.put(f.getName(), f.get(entity));
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        map.put("data", subMap);
        map.put("key", entity.getId());
        return baseMapper.update(map);
    }

    @Override
    public List<T> findAll() {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        List<T> list =  baseMapper.find_list(map);
        return list;
    }

    @Override
    public List<T> findAll(Collection<ID> keys) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("keys", keys);
        List<T> list =  baseMapper.find_list(map);
        return list;
    }

    @Override
    public T findOne(Serializable id) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        map.put("key", id);
        T entity =  (T) baseMapper.find_one(map);
        return entity;
    }

    @Override
    public PageInfo<T> findPage(int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("__tableName__", tableName);
        map.put("__entityClass__",entityClass);
        PageHelper.startPage(pageNum, pageSize);
        List<T> list =  baseMapper.find_list(map);
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
        List<T> list =  baseMapper.find_list(map);
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