package com.testwa.core.base.mybatis.plugin;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.testwa.core.base.mybatis.annotation.VersionLocker;
import com.testwa.core.base.mybatis.builder.TableMataDate;
import com.testwa.core.base.mybatis.util.Constent;
import com.testwa.core.base.mybatis.util.PluginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;


@Slf4j
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
    @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}
)})
public class OptimisticLocker implements Interceptor {

    private String versionColumn;

    @Override
    public void setProperties(Properties properties) {
        versionColumn = properties.getProperty("versionColumn", "version");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object intercept(Invocation invocation) throws Exception {

        String interceptMethod = invocation.getMethod().getName();
        if ("prepare".equals(interceptMethod)) {

            StatementHandler routingHandler = (StatementHandler) PluginUtil.processTarget(invocation.getTarget());
            MetaObject routingMeta = SystemMetaObject.forObject(routingHandler);
            MetaObject hm = routingMeta.metaObjectForProperty("delegate");

            VersionLocker vl = VersionLockerResolver.resolve(hm);
            if (null != vl && !vl.value()) {
                return invocation.proceed();
            }
            BoundSql boundSql = routingHandler.getBoundSql();

            String originalSql = boundSql.getSql();
            StringBuilder builder = new StringBuilder(originalSql);
            builder.append(" AND ");
            builder.append(versionColumn);
            builder.append(" = ?");
//            hm.setValue("boundSql.sql", builder.toString());
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, builder.toString());


        } else if ("setParameters".equals(interceptMethod)) {

            ParameterHandler handler = (ParameterHandler) PluginUtil.processTarget(invocation.getTarget());
            MetaObject hm = SystemMetaObject.forObject(handler);

            VersionLocker vl = VersionLockerResolver.resolve(hm);
            if (null != vl && !vl.value()) {
                return invocation.proceed();
            }

            BoundSql boundSql = (BoundSql) hm.getValue("boundSql");
            Object parameterObject = boundSql.getParameterObject();
            if (parameterObject instanceof MapperMethod.ParamMap<?>) {
                MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) parameterObject;
                if (!paramMap.containsKey(versionColumn)) {
                    throw new TypeException("All the primitive type parameters must add MyBatis's @Param Annotaion");
                }
            }

            Configuration configuration = ((MappedStatement) hm.getValue("mappedStatement")).getConfiguration();
            MetaObject pm = configuration.newMetaObject(parameterObject);

            Object value = null;
            AtomicReference<String> versionField = new AtomicReference<>();
            if(pm.getOriginalObject() instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) pm.getOriginalObject();
                value = paramMap.get(versionColumn);
                versionField.set(versionColumn);
            }else{
                TableMataDate mataDate = TableMataDate.forClass(pm.getOriginalObject().getClass());

                mataDate.getFieldColumnMap().forEach( (k, v) -> {
                    if(v.equals(versionColumn)) {
                        versionField.set(k);
                    }
                });
                value = pm.getValue(versionField.get());
            }

            ParameterMapping versionMapping = new ParameterMapping.Builder(configuration, versionColumn, Object.class).build();
            TypeHandler typeHandler = versionMapping.getTypeHandler();
            JdbcType jdbcType = versionMapping.getJdbcType();

            if (value == null && jdbcType == null) {
                jdbcType = configuration.getJdbcTypeForNull();
            }

            int versionLocation = boundSql.getParameterMappings().size() + 1;
            try {
                PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
                typeHandler.setParameter(ps, versionLocation, value, jdbcType);
            } catch (TypeException | SQLException e) {
                throw new TypeException("set parameter 'version' faild, Cause: " + e, e);
            }

            if (!Objects.equals(value.getClass(), Long.class) && Objects.equals(value.getClass(), long.class) && log.isDebugEnabled()) {
                log.error(Constent.LOG_PREFIX + "property type error, the type of version property must be Long or long.");
            }

            // increase version
            pm.setValue(versionField.get(), (long) value + 1);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler || target instanceof ParameterHandler)
            return Plugin.wrap(target, this);
        return target;
    }

}