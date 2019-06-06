package com.bill.volley.toolbox;

import com.bill.volley.Cache;
import com.bill.volley.Header;
import com.bill.volley.Network;
import com.bill.volley.NetworkResponse;
import com.bill.volley.Request;
import com.bill.volley.VolleyError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 执行网络请求，调用{@link HttpStack} 并返回 {@link NetworkResponse} 对象
 */
public class BasicNetwork implements Network {

    private AdaptedHttpStack adaptedHttpStack;

    public BasicNetwork(HttpStack httpStack) {
        adaptedHttpStack = new AdaptedHttpStack(httpStack);
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        while (true) {
            HttpResponse httpResponse;
            byte[] responseContents;
            List<Header> responseHeaders;
            try {
                Map<String, String> additionalRequestHeaders = getCacheHeaders(request.getCacheEntry());
                httpResponse = adaptedHttpStack.executeRequest(request, additionalRequestHeaders);

                int statusCode = httpResponse.getStatusCode();

                responseHeaders = httpResponse.getHeaders();

                InputStream inputStream = httpResponse.getContent();

                if (inputStream != null) {
                    responseContents =
                            inputStreamToBytes(inputStream, httpResponse.getContentLength());
                } else {
                    responseContents = new byte[0];
                }

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }

                return new NetworkResponse(
                        statusCode,
                        responseContents,
                        responseHeaders);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] inputStreamToBytes(InputStream in, int contentLength)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(contentLength);
        byte[] buffer = new byte[1024];
        try {
            int count;
            while ((count = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } finally {
            in.close();
            outputStream.close();
        }
    }

    private Map<String, String> getCacheHeaders(Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();

        return headers;
    }

}
