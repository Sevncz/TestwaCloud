package com.testwa.core.base.mybatis.builder;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Id;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.core.base.util.NameUtils;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TableMataDate {

    private static final Map<Class<?>, TableMataDate> TABLE_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 表名
     */
    private String tableName;

    /**
     * 主键属性名
     */
    private String pkProperty;

    /**
     * 主键对应的列名
     */
    private String pkColumn;

    /**
     * 属性名和字段名映射关系的 map
     */
    private Map<String, String> fieldColumnMap;

    /**
     * 字段类型
     */
    private Map<String, Class<?>> fieldTypeMap;

    private TableMataDate(Class<?> clazz) {
        fieldColumnMap = new HashMap<>();
        fieldTypeMap = new HashMap<>();
        initTableInfo(clazz);
    }


    public static TableMataDate forClass(Class<?> entityClass) {
        TableMataDate tableMataDate = TABLE_CACHE.get(entityClass);
        if (tableMataDate == null) {
            tableMataDate = new TableMataDate(entityClass);
            TABLE_CACHE.put(entityClass, tableMataDate);
        }

        return tableMataDate;
    }

    public String getBaseColumns() {
        Collection<String> columns = fieldColumnMap.values();
        if (CollectionUtils.isEmpty(columns)) {
            return "";
        }
        Iterator<String> iterator = columns.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String next = iterator.next();
            sb.append(tableName).append(".").append(next);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * 根据注解初始化表信息，
     *
     * @param clazz 实体类的 class
     */
    private void initTableInfo(Class<?> clazz) {
        tableName = clazz.isAnnotationPresent(Table.class) ? clazz.getAnnotation(Table.class).name()
                : NameUtils.getUnderLineName(clazz.getSimpleName());
        List<Field> fieldList = new ArrayList<>() ;
        Class tempClass = clazz;
        while (tempClass != null) { // 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }

        for (Field field : fieldList) {

            // 过滤静态字段和有 @Transient 注解的字段
            if (Modifier.isStatic(field.getModifiers()) ||
                    field.isAnnotationPresent(Transient.class) ||
                    !BeanUtils.isSimpleValueType(field.getType())) {
                continue;
            }

            String property = field.getName();
            Column column = field.getAnnotation(Column.class);
            String columnName = column != null ? column.name() : NameUtils.getUnderLineName(property);

            // 主键信息 : 有 @Id 注解的字段，没有默认是 类名+Id
            if (field.isAnnotationPresent(Id.class) || (property.equalsIgnoreCase("id") && pkProperty == null)) {
                pkProperty = property;
                pkColumn = columnName;
            }
            // 将字段对应的列放到 map 中
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, property);
            if (descriptor != null && descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null) {
                fieldColumnMap.put(property, columnName);
                fieldTypeMap.put(property, field.getType());
            }
        }
    }

}