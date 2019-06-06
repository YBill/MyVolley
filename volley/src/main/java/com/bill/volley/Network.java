package com.bill.volley;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 执行网络请求
 */
public interface Network {

    /**
     * 调用网络请求 {@link com.bill.volley.toolbox.HttpStack}
     *
     * @param request 网络请求数据
     * @return 返回 {@link NetworkResponse} 包含网络请求的数据，头部和请求码
     * @throws VolleyError
     */
    NetworkResponse performRequest(Request<?> request) throws VolleyError;

}
