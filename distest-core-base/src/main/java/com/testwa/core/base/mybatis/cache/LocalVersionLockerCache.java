package com.testwa.core.base.mybatis.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.testwa.core.base.mybatis.annotation.VersionLocker;
import com.testwa.core.base.mybatis.util.Constent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalVersionLockerCache implements VersionLockerCache {

    private ConcurrentHashMap<String, ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker>> caches = new ConcurrentHashMap<>();

    @Override
    public boolean containMethodSignature(MethodSignature ms) {
        String nameSpace = getNameSpace(ms);
        ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (null == cache || cache.isEmpty()) {
            return false;
        }
        boolean containsMethodSignature = cache.containsKey(ms);
        if (containsMethodSignature && log.isDebugEnabled()) {
            log.debug(Constent.LOG_PREFIX + "The method " + nameSpace + ms.getId() + "is hit in cache.");
        }
        return containsMethodSignature;
    }

    @Override
    public void cacheMethod(VersionLockerCache.MethodSignature vm, VersionLocker locker) {
        String nameSpace = getNameSpace(vm);
        ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (null == cache || cache.isEmpty()) {
            cache = new ConcurrentHashMap<>();
            cache.put(vm, locker);
            caches.put(nameSpace, cache);
            if (log.isDebugEnabled()) {
                log.debug(Constent.LOG_PREFIX + nameSpace + ": " + vm.getId() + " is cached.");
            }
        } else {
            cache.put(vm, locker);
        }
    }

    @Override
    public VersionLocker getVersionLocker(VersionLockerCache.MethodSignature vm) {
        String nameSpace = getNameSpace(vm);
        ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
        if (null == cache || cache.isEmpty()) {
            return null;
        }
        return cache.get(vm);
    }

    private String getNameSpace(VersionLockerCache.MethodSignature vm) {
        String id = vm.getId();
        int pos = id.lastIndexOf('.');
        return id.substring(0, pos);
    }

}
