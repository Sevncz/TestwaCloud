package com.testwa.core.base.mybatis.cache;

import com.testwa.core.base.mybatis.annotation.VersionLocker;

import java.util.Arrays;


public interface Cache<T> {

    boolean containMethodSignature(VersionLockerCache.MethodSignature vm);

    void cacheMethod(VersionLockerCache.MethodSignature vm, VersionLocker locker);

    T getVersionLocker(VersionLockerCache.MethodSignature vm);

    class MethodSignature {

        private String id;
        private Class<?>[] params;

        public MethodSignature(String id, Class<?>[] params) {
            this.id = id;
            this.params = params;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Class<?>[] getParams() {
            return params;
        }

        public void setParams(Class<?>[] params) {
            this.params = params;
        }

        @Override
        public int hashCode() {
            int idHash = id.hashCode();
            int paramsHash = Arrays.hashCode(params);
            return ((idHash >> 16 ^ idHash) << 16) | (paramsHash >> 16 ^ paramsHash);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodSignature)) {
                return super.equals(obj);
            }
            MethodSignature ms = (MethodSignature) obj;
            // 对同一个方法的判断：1、方法名相同；2、参数列表相同
            // if the method signature is 'equal', must 2 conditions: 1.the method name be
            // the same; 2.the parameters type be the same
            return id.equals(ms.id) && Arrays.equals(params, ms.params);
        }

    }
}