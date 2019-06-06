package com.bill.volley;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 定义了外部使用的接口，成功和失败数据
 *
 * @param <T>
 */
public class Response<T> {

    public interface Listener<T> {
        void onResponse(T response);
    }

    public interface ErrorListener {
        void onErrorResponse(VolleyError error);
    }

    public static <T> Response<T> success(T result, Cache.Entry cacheEntry) {
        return new Response<>(result, cacheEntry);
    }

    public static <T> Response<T> error(VolleyError error) {
        return new Response<>(error);
    }

    public final T result;

    public final Cache.Entry cacheEntry;

    public final VolleyError error;

    public boolean intermediate = false;

    public Response(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }

    public Response(VolleyError volleyError) {
        this.result = null;
        this.cacheEntry = null;
        this.error = volleyError;
    }

    public boolean isSuccess() {
        return error == null;
    }

}
