package com.developerhaoz.androidnetwork.volley;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 一个用 String 作为 key，将字节数组作为数据的缓存接口
 *
 * @author Haoz
 * @date 2018/1/30.
 */
public interface Cache {

    Entry get(String key);

    void put(String key, Entry entry);

    void initialize();

    void invalidate(String key, boolean fullExpire);

    void remove(String key);

    void clear();

    /**
     * 从 Cache 返回的包含数据和元数据的类
     */
    class Entry{


        /** 从 Cache 中返回的数据 */
        public byte[] data;

        public String etag;

        public long serverDate;

        public long lastModified;

        public long ttl;

        public long softTtl;

        public Map<String, String> responseHeaders = Collections.emptyMap();

        public List<Header> allResponseHeaders;

        public boolean isExpired(){
            return this.ttl < System.currentTimeMillis();
        }

        public boolean refreshNeeded(){
            return this.softTtl < System.currentTimeMillis();
        }
    }
}















