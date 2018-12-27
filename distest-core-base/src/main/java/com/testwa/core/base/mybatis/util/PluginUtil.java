package com.testwa.core.base.mybatis.util;

import java.lang.reflect.Proxy;

import com.testwa.core.base.mybatis.exception.LockerException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public final class PluginUtil {

    private static final Log log = LogFactory.getLog(PluginUtil.class);

    private PluginUtil() {} // private constructor

    /**
     * <p>
     * Recursive get the original target object.
     * <p>
     * If integrate more than a plugin, maybe there are conflict in these plugins,
     * because plugin will proxy the object.<br>
     * So, here get the orignal target object
     * 
     * @param target proxy-object
     * @return original target object
     */
    public static Object processTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject mo = SystemMetaObject.forObject(target);
            return processTarget(mo.getValue("h.target"));
        }

        // must keep the result object is StatementHandler or ParameterHandler in
        // Optimistic Loker plugin
        if (!(target instanceof StatementHandler) && !(target instanceof ParameterHandler)) {
            if (log.isDebugEnabled()) {
                log.error(Constent.LOG_PREFIX + "plugin init faild.");
            }
            throw new LockerException(Constent.LOG_PREFIX + "plugin init faild.");
        }
        return target;
    }

}