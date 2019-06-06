package com.bill.volley.toolbox;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import com.bill.volley.NetworkResponse;
import com.bill.volley.Request;
import com.bill.volley.Response;

import java.io.UnsupportedEncodingException;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 将网络请求转换为String返回
 */
public class StringRequest extends Request<String> {

    private final Object mLock = new Object();

    @Nullable
    @GuardedBy("mLock")
    private Response.Listener<String> mListener;

    public StringRequest(int method,
                         String url,
                         Response.Listener<String> listener,
                         @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }

    @Override
    protected void deliverResponse(String response) {
        Response.Listener<String> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
