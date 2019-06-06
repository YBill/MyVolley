package com.bill.volley.toolbox;

import com.bill.volley.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 内存缓存实现，只存在内存中
 */
public class MemoryCache implements Cache {

    private final Map<String, Cache.Entry> cache = new LinkedHashMap<>();

    @Override
    public Entry get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, Entry entry) {
        long putTime = System.currentTimeMillis();
        entry.ttl = putTime;
        cache.put(key, entry);
    }

    @Override
    public void initialize() {

    }

    @Override
    public Entry remove(String key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
