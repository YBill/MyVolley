package com.bill.volley.toolbox;

import com.bill.volley.Request;
import com.bill.volley.VolleyError;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 真正执行网络请求，有其他方式实现网络请求 HttpStack 即可
 */
public interface HttpStack {

    /**
     * 真正的执行网络请求
     *
     * @param request           请求信息
     * @param additionalHeaders 额外的头
     * @return 返回的是系统的网络请求类 {@link org.apache.http.HttpResponse}
     * @throws IOException
     * @throws VolleyError
     */
    HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, VolleyError;

}
