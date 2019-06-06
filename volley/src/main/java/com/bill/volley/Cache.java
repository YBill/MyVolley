package com.bill.volley;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 缓存接口
 */
public interface Cache {

    Entry get(String key);

    void put(String key, Entry entry);

    void initialize();

    Entry remove(String key);

    void clear();

    class Entry {
        public byte[] data;

        public Map<String, String> responseHeaders = Collections.emptyMap();

        public long ttl;

        public boolean isExpired() {
            // 超出expiredTime即视为过期
            long expiredTime = 60 * 60 * 1000; // 60分钟
            long time = System.currentTimeMillis() - this.ttl;
            return time > expiredTime;
        }
    }
}

