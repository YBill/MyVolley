package com.bill.volley;

import android.support.annotation.CallSuper;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 网络请求的包装，内部有网络请求的所以参数
 *
 * @param <T>
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public void deliverError(VolleyError error) {
        Response.ErrorListener listener;
        synchronized (mLock) {
            listener = mErrorListener;
        }
        if (listener != null) {
            listener.onErrorResponse(error);
        }
    }

    protected abstract void deliverResponse(T response);

    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    public interface Method {
        int GET = 0;
        int POST = 1;

    }

    private final int mMethod;

    private final String mUrl;

    private final Object mLock = new Object();

    private Cache.Entry mCacheEntry = null;

    private boolean mShouldCache = true;

    private RequestQueue mRequestQueue;

    @GuardedBy("mLock")
    private boolean mCanceled = false;

    @GuardedBy("mLock")
    private boolean mResponseDelivered = false;

    @Nullable
    @GuardedBy("mLock")
    private Response.ErrorListener mErrorListener;

    public Request(int method, String url, @Nullable Response.ErrorListener listener) {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;

    }

    public String getUrl() {
        return mUrl;
    }

    public int getMethod() {
        return mMethod;
    }

    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    public byte[] getBody() throws VolleyError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    public String getCacheKey() {
        String url = getUrl();
        int method = getMethod();
        if (method == Method.GET) {
            return url;
        }
        return Integer.toString(method) + '-' + url;
    }

    /**
     * Returns true if responses to this request should be cached.
     */
    public final boolean shouldCache() {
        return mShouldCache;
    }

    public final int getTimeoutMs() {
        return 2500;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    protected Map<String, String> getParams() throws VolleyError {
        return null;
    }

    public Map<String, String> getHeaders() throws VolleyError {
        return Collections.emptyMap();
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Request#getParams() or Request#getPostParams() returned a map "
                                            + "containing a null key or value: (%s, %s). All keys "
                                            + "and values must be non-null.",
                                    entry.getKey(), entry.getValue()));
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    public Request<?> setCacheEntry(Cache.Entry entry) {
        mCacheEntry = entry;
        return this;
    }

    public final Request<?> setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }

    public void markDelivered() {
        synchronized (mLock) {
            mResponseDelivered = true;
        }
    }

    public boolean hasHadResponseDelivered() {
        synchronized (mLock) {
            return mResponseDelivered;
        }
    }

    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return volleyError;
    }

    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    @CallSuper
    public void cancel() {
        synchronized (mLock) {
            mCanceled = true;
            mErrorListener = null;
        }
    }

    /**
     * Returns true if this request has been canceled.
     */
    public boolean isCanceled() {
        synchronized (mLock) {
            return mCanceled;
        }
    }

    void finish() {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public int compareTo(@NonNull Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();
        return right.ordinal() - left.ordinal();
    }
}
