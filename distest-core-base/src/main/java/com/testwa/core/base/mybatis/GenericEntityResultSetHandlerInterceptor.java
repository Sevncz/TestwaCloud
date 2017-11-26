package com.testwa.core.base.mybatis;

import com.testwa.core.base.bo.Entity;
import com.testwa.core.base.util.ReflectUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Properties;


@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class GenericEntityResultSetHandlerInterceptor implements Interceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement stmt = (MappedStatement) invocation.getArgs()[0];
        ResultMap resultMap = stmt.getResultMaps().get(0);
        Class resultType = resultMap.getType();
        if(resultType!=null && resultType == Entity.class) {
            Map<String, Object> params = (Map<String, Object>) invocation.getArgs()[1];
            ReflectUtil.setFieldValue(resultMap, "type", params.get("__entityClass__"));
            Object o = invocation.proceed();
            ReflectUtil.setFieldValue(resultMap, "type", Entity.class);
            return o;
        }
        return invocation.proceed();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }


}