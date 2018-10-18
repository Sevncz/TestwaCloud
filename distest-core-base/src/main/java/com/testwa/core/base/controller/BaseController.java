package com.testwa.core.base.controller;

import com.testwa.core.base.vo.ResultVO;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.vo.PageResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        this.context = arg0;
    }

    /**
     * 返回失败
     * @param code
     * @param message
     * @return
     */
    public ResultVO<String> fail(ResultCode code, String message) {
        ResultVO<String> r = new ResultVO<>();
        r.setCode(code.getValue());
        r.setType(code.name());
        r.setMessage(message);
        return r;
    }

    /**
     * 返回成功，不带数据
     * @return
     */
    public ResultVO<String> ok() {
        ResultVO<String> r = new ResultVO<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setType(ResultCode.SUCCESS.name());
        r.setMessage("ok");
        return r;
    }

    /**
     * 返回成功，带数据
     * @param data
     * @return
     */
    public ResultVO<Object> ok(Object data) {
        ResultVO<Object> r = new ResultVO<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setType(ResultCode.SUCCESS.name());
        if(data != null){
            r.setData(data);
        }
        r.setMessage("ok");
        return r;
    }


    protected <T, E> PageResultVO<E> buildVOPageResult(PageResultVO<T> entityPR, Class<E> vo) {
        List<E> vos = buildVOs(entityPR.getPages(), vo);
        PageResultVO<E> pr = new PageResultVO<>(vos, entityPR.getTotal());
        return pr;
    }

    protected <T, E> List<E> buildVOs(List<T> entityList, Class<E> c) {
        List<E> vos = new ArrayList<>();
        entityList.forEach(entity -> {
            E e = buildVO(entity, c);
            vos.add(e);
        });
        return vos;
    }

    protected <T, E> E buildVO(T entity, Class<E> c) {
        E vo = null;
        try {
            vo = c.newInstance();
            BeanUtils.copyProperties(entity, vo);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return vo;
    }

    public final boolean validTimestamp(long ts) {
        return ts >= MIN_TIMESTAMP && ts <= System.currentTimeMillis();
    }

}
