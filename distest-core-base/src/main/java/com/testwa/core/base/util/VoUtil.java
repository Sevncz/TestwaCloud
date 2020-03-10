package com.testwa.core.base.util;

import com.testwa.core.base.vo.PageResult;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class VoUtil {

    public static <T, E> PageResult<E> buildVOPageResult(PageResult<T> entityPR, Class<E> vo) {
        List<E> vos = buildVOs(entityPR.getPages(), vo);
        return new PageResult<>(vos, entityPR.getTotal());
    }

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
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
