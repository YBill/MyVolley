package com.bill.volley;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 用来交付网络响应的
 */
public interface ResponseDelivery {

    /**
     * 交付成功请求
     *
     * @param request
     * @param response
     */
    void postResponse(Request<?> request, Response<?> response);

    /**
     * 交付失败请求
     *
     * @param request
     * @param error
     */
    void postError(Request<?> request, VolleyError error);
}
