package com.bill.myvolley;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import com.bill.volley.NetworkResponse;
import com.bill.volley.Request;
import com.bill.volley.Response;
import com.bill.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Bill on 2019/6/5.
 */
public class GsonRequest<T> extends Request<T> {

    private final Object mLock = new Object();
    private final Class<T> mCls;

    @Nullable
    @GuardedBy("mLock")
    private Response.Listener<T> mListener;

    public GsonRequest(
            int method,
            String url,
            Class<T> cls,
            Response.Listener<T> listener,
            @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mCls = cls;
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            // Since minSdkVersion = 8, we can't call
            // new String(response.data, Charset.defaultCharset())
            // So suppress the warning instead.
            parsed = new String(response.data);
        }

        String data = "";
        try {
            JSONObject obj = new JSONObject(parsed);
            data = obj.optJSONObject("data").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        T t = gson.fromJson(data, mCls);

        return Response.success(t, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response) {
        Response.Listener<T> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }
}
