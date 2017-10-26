package com.testwa.distest.common.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.ResultCode;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.entity.Testcase;
import com.testwa.distest.server.web.app.vo.AppVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


public class BaseController implements ApplicationContextAware {

    protected ApplicationContext context;

    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        this.context = arg0;
    }

    /**
     * 返回失败
     * @param code
     * @param message
     * @return
     */
    public Result<String> fail(ResultCode code, String message) {
        Result<String> r = new Result<>();
        r.setCode(code.getValue());
        r.setMessage(message);
        return r;
    }

    /**
     * 返回成功，不带数据
     * @return
     */
    public Result<String> ok() {
        Result<String> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setMessage("ok");
        return r;
    }

    /**
     * 返回成功，带数据
     * @param data
     * @return
     */
    public Result<Object> ok(Object data) {
        Result<Object> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getValue());
        r.setData(data);
        r.setMessage("ok");
        return r;
    }


    protected <T, E> PageResult<E> buildVOPageResult(PageResult<T> entityPR, Class<E> vo) {
        List<E> vos = buildVOs(entityPR.getPages(), vo);
        PageResult<E> pr = new PageResult<>(vos, entityPR.getTotal());
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

}
