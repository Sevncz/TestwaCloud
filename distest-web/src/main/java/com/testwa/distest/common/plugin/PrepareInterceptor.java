package com.testwa.distest.common.plugin;

import com.testwa.core.base.mybatis.builder.TableMataDate;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.common.util.ApplicationContextUtil;
import com.testwa.distest.server.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class PrepareInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
//        SnowflakeIdWorker commonIdWorker = (SnowflakeIdWorker) ApplicationContextUtil.getBean("commonIdWorker");
        User currentUser = (User) ApplicationContextUtil.getBean("user");
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //注解中method的值
        String methodName = invocation.getMethod().getName();
        //sql类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        // 获取参数
        Object parameter = invocation.getArgs()[1];
        // 获取私有成员变量
        TableMataDate mataDate = TableMataDate.forClass(parameter.getClass());

        if ("update".equals(methodName)) {
            Date currentDate = new Date(System.currentTimeMillis());
            //对有要求的字段填值
            if (SqlCommandType.INSERT.equals(sqlCommandType)) {
//                Field fieldId = mataDate.getFieldMap().get(mataDate.getPkColumn());
//                fieldId.setAccessible(true);
//                long id = commonIdWorker.nextId();
//                fieldId.set(parameter, id);
//                log.info("插入操作前设置id:{}", id);
                Field fieldCreateTime = mataDate.getFieldMap().get("createTime");
                if(fieldCreateTime != null) {
                    fieldCreateTime.setAccessible(true);
                    fieldCreateTime.set(parameter, currentDate);
                    log.info("插入操作时设置create_time:{}", currentDate);
                }
                Field fieldCreateBy = mataDate.getFieldMap().get("createBy");
                if(fieldCreateBy != null) {
                    fieldCreateBy.setAccessible(true);
                    fieldCreateBy.set(parameter, currentUser.getId());
                    log.info("插入操作时设置create_by:{}", currentDate);
                }
                Field fieldEnabled = mataDate.getFieldMap().get("enabled");
                if(fieldEnabled != null) {
                    fieldEnabled.setAccessible(true);
                    Object valueEnabeld = fieldEnabled.get(parameter);
                    if (valueEnabeld == null) {
                        fieldEnabled.set(parameter, true);
                        log.info("插入操作时设置enabled: 1");
                    }
                }
                Field fieldLockVersion = mataDate.getFieldMap().get("lockVersion");
                if(fieldLockVersion != null) {
                    fieldLockVersion.setAccessible(true);
                    Object valueLockVersion = fieldLockVersion.get(parameter);
                    if (valueLockVersion == null) {
                        fieldLockVersion.set(parameter, 1L);
                        log.info("插入操作时设置lockVersion: 1");
                    }
                }
                Field fieldModifyTime = mataDate.getFieldMap().get("updateTime");
                if(fieldModifyTime != null) {
                    fieldModifyTime.setAccessible(true);
                    fieldModifyTime.set(parameter, currentDate);
                    log.info("更新操作时设置update_time:{}", currentDate);
                }
                Field fieldUpdateBy = mataDate.getFieldMap().get("updateBy");
                if(fieldUpdateBy != null) {
                    fieldUpdateBy.setAccessible(true);
                    fieldUpdateBy.set(parameter, currentUser.getId());
                    log.info("更新操作时设置update_by:{}", currentDate);
                }
            } else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
                Field fieldModifyTime = mataDate.getFieldMap().get("updateTime");
                if(fieldModifyTime != null) {
                    fieldModifyTime.setAccessible(true);
                    fieldModifyTime.set(parameter, currentDate);
                    log.info("更新操作时设置update_time:{}", currentDate);
                }
                Field fieldUpdateBy = mataDate.getFieldMap().get("updateBy");
                if(fieldUpdateBy != null) {
                    fieldUpdateBy.setAccessible(true);
                    fieldUpdateBy.set(parameter, currentUser.getId());
                    log.info("更新操作时设置update_by:{}", currentDate);
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}