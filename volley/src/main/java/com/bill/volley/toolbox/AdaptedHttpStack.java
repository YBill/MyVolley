package com.bill.volley.toolbox;

import com.bill.volley.Header;
import com.bill.volley.Request;
import com.bill.volley.VolleyError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 网络请求的适配器，用来分发网络请求，将返回的 {@link org.apache.http.HttpResponse} 对象包装为内部的 {@link HttpResponse}
 */
public class AdaptedHttpStack {

    private final HttpStack mHttpStack;

    AdaptedHttpStack(HttpStack httpStack) {
        mHttpStack = httpStack;
    }

    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, VolleyError {
        org.apache.http.HttpResponse apacheResp = mHttpStack.performRequest(request, additionalHeaders);

        int statusCode = apacheResp.getStatusLine().getStatusCode();

        org.apache.http.Header[] headers = apacheResp.getAllHeaders();
        List<Header> headerList = new ArrayList<>(headers.length);
        for (org.apache.http.Header header : headers) {
            headerList.add(new Header(header.getName(), header.getValue()));
        }

        if (apacheResp.getEntity() == null) {
            return new HttpResponse(statusCode, headerList);
        }

        long contentLength = apacheResp.getEntity().getContentLength();
        if ((int) contentLength != contentLength) {
            throw new IOException("Response too large: " + contentLength);
        }

        return new HttpResponse(
                statusCode,
                headerList,
                (int) apacheResp.getEntity().getContentLength(),
                apacheResp.getEntity().getContent());
    }

}
