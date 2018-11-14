package com.testwa.core.base.controller;

import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.core.base.constant.ResultCode;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class BaseController implements ApplicationContextAware {

    protected ApplicationContext context;
    private static final String RELEASE_DATE ="2018-01-01";

    private static final long MIN_TIMESTAMP;

    static {
        try {
            MIN_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd").parse(RELEASE_DATE).getTime();
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    public void setApplicationContext(ApplicationContext arg0) {
        this.context = arg0;
    }

    protected <T, E> PageResult<E> buildVOPageResult(PageResult<T> entityPR, Class<E> vo) {
        List<E> vos = buildVOs(entityPR.getPages(), vo);
        return new PageResult<>(vos, entityPR.getTotal());
    }

    protected <T, E> List<E> buildVOs(List<T> entityList, Class<E> c) {
        List<E> result = new ArrayList<>();
        entityList.forEach(entity -> {
            E e = buildVO(entity, c);
            result.add(e);
        });
        return result;
    }

    protected <T, E> E buildVO(T entity, Class<E> c) {
        E result = null;
        try {
            result = c.newInstance();
            BeanUtils.copyProperties(result, entity);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return result;
    }

    public final boolean validTimestamp(long ts) {
        return ts >= MIN_TIMESTAMP && ts <= System.currentTimeMillis();
    }

}
