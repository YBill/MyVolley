package com.bill.volley.toolbox;

import com.bill.volley.Request;
import com.bill.volley.VolleyError;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 真正的网络请求，内部通过 {@link HttpClient} 实现
 */
public class HttpClientStack implements HttpStack {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final HttpClient mClient;

    public HttpClientStack(HttpClient client) {
        mClient = client;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, VolleyError {
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        setHeaders(httpRequest, additionalHeaders);
        setHeaders(httpRequest, request.getHeaders());
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        return mClient.execute(httpRequest);
    }

    static HttpUriRequest createHttpRequest(
            Request<?> request, Map<String, String> additionalHeaders) throws VolleyError {
        switch (request.getMethod()) {
            case Request.Method.GET:
                return new HttpGet(request.getUrl());
            case Request.Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(
            HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
            throws VolleyError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    private static void setHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

}
