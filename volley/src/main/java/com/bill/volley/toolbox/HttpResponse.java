package com.bill.volley.toolbox;

import com.bill.volley.Header;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 内部封装的网络响应类，包含网络返回的信息
 */
public final class HttpResponse {

    private final int mStatusCode;
    private final List<Header> mHeaders;
    private final int mContentLength;
    private final InputStream mContent;

    public HttpResponse(int statusCode, List<Header> headers) {
        this(statusCode, headers, -1, null);
    }

    public HttpResponse(
            int statusCode, List<Header> headers, int contentLength, InputStream content) {
        mStatusCode = statusCode;
        mHeaders = headers;
        mContentLength = contentLength;
        mContent = content;
    }

    public final int getStatusCode() {
        return mStatusCode;
    }

    public final List<Header> getHeaders() {
        return Collections.unmodifiableList(mHeaders);
    }

    public final int getContentLength() {
        return mContentLength;
    }

    public final InputStream getContent() {
        return mContent;
    }
}
