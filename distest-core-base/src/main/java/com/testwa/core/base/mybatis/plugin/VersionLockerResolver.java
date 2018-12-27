package com.testwa.core.base.mybatis.plugin;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.testwa.core.base.mybatis.cache.Cache;
import com.testwa.core.base.mybatis.cache.LocalVersionLockerCache;
import com.testwa.core.base.mybatis.cache.VersionLockerCache;
import com.testwa.core.base.mybatis.exception.LockerException;
import com.testwa.core.base.mybatis.util.Constent;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import com.testwa.core.base.mybatis.annotation.VersionLocker;

@Slf4j
class VersionLockerResolver {

    /** versionLockerCache, mapperMap are ConcurrentHashMap, thread safe **/
    private static final VersionLockerCache versionLockerCache = new LocalVersionLockerCache();
    private static final Map<String, Class<?>> mapperMap = new ConcurrentHashMap<>();

    private static final VersionLocker trueLocker;
    private static final VersionLocker falseLocker;
    static {
        try {
            trueLocker = VersionLockerResolver.class.getDeclaredMethod("trueVersionValue").getAnnotation(VersionLocker.class);
            falseLocker = VersionLockerResolver.class.getDeclaredMethod("falseVersionValue").getAnnotation(VersionLocker.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LockerException("Optimistic Locker Plugin init faild." + e, e);
        }
    }

    @VersionLocker(true)
    private void trueVersionValue() {
        // no thing to do.
    }

    @VersionLocker(false)
    private void falseVersionValue() {
        // no thing to do.
    }

    static VersionLocker resolve(MetaObject mo) {

        // if the method is not a 'update', return false
        MappedStatement ms = (MappedStatement) mo.getValue("mappedStatement");
        if (!Objects.equals(ms.getSqlCommandType(), SqlCommandType.UPDATE))
            return falseLocker;

        BoundSql boundSql = (BoundSql) mo.getValue("boundSql");
        Object paramObj = boundSql.getParameterObject();
        Class<?>[] paramCls = null;

        /****************** Process param must order by below ***********************/
        // 1、Process @Param param
        if (paramObj instanceof MapperMethod.ParamMap<?>) {
            MapperMethod.ParamMap<?> mmp = (MapperMethod.ParamMap<?>) paramObj;
            if (!mmp.isEmpty()) {
                paramCls = new Class<?>[mmp.size() >> 1];
                int mmpLen = mmp.size() >> 1;
                for (int i = 0; i < mmpLen; i++) {
                    Object index = mmp.get("param" + (i + 1));
                    paramCls[i] = index.getClass();
                }
            }

            // 2、Process Map param
        } else if (paramObj instanceof Map) {
            paramCls = new Class<?>[] {Map.class};

            // 3、Process POJO entity param
        } else {
            paramCls = new Class<?>[] {paramObj.getClass()};
        }

        String id = ms.getId();
        Cache.MethodSignature vm = new Cache.MethodSignature(id, paramCls);
        VersionLocker versionLocker = versionLockerCache.getVersionLocker(vm);
        if (null != versionLocker)
            return versionLocker;

        if (mapperMap.isEmpty()) {
            Collection<Class<?>> mappers = ms.getConfiguration().getMapperRegistry().getMappers();
            if (null != mappers && !mappers.isEmpty()) {
                for (Class<?> me : mappers) {
                    mapperMap.put(me.getName(), me);
                }
            }
        }

        int pos = id.lastIndexOf('.');
        String nameSpace = id.substring(0, pos);
        if (!mapperMap.containsKey(nameSpace) && log.isDebugEnabled()) {
            log.debug(Constent.LOG_PREFIX + "Config info error, maybe you have not config the Mapper interface");
            throw new LockerException("Config info error, maybe you have not config the Mapper interface");
        }
        Class<?> mapper = mapperMap.get(nameSpace);
        Method m;
        try {
            m = getDeclaredMethod(mapper, id.substring(pos + 1), paramCls);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LockerException("The Map type param error." + e, e);
        }
        versionLocker = m.getAnnotation(VersionLocker.class);
        if (null == versionLocker) {
            versionLocker = falseLocker;
        }
        if (!versionLockerCache.containMethodSignature(vm)) {
            versionLockerCache.cacheMethod(vm, versionLocker);
        }
        return versionLocker;
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?> ... parameterTypes) throws NoSuchMethodException {
        Method method = null ;
        Class tempClass = clazz;
        while (tempClass != null) { // 当父类为null的时候说明到达了最上层的父类(Object类).
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes) ;
                return method ;
            } catch (Exception e) {
            }
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        Class[] interfaces = clazz.getInterfaces();
        for(Class c : interfaces) {
            Method[] methods = c.getMethods();
            for(Method m : methods) {
                if(m.getName().equals(methodName)) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException();
    }

}