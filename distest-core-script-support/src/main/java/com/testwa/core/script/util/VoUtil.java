package com.testwa.core.script.util;


import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class VoUtil {

    public static <T, E> List<E> buildVOs(List<T> entityList, Class<E> c) {
        List<E> result = new ArrayList<>();
        entityList.forEach(entity -> {
            E e = buildVO(entity, c);
            result.add(e);
        });
        return result;
    }

    public static <T, E> E buildVO(T entity, Class<E> c) {
        E result = null;
        try {
            result = c.newInstance();
            BeanUtils.copyProperties(result, entity);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
