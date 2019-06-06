package com.bill.volley;

/**
 * Created by Bill on 2019/6/5.
 */
public class VolleyError extends Exception {

    public final NetworkResponse mNetworkResponse;
    private long mNetworkTimeMs;

    public VolleyError() {
        mNetworkResponse = null;
    }

    public VolleyError(NetworkResponse response) {
        mNetworkResponse = response;
    }

    public VolleyError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        mNetworkResponse = null;
    }

    public VolleyError(Throwable cause) {
        super(cause);
        mNetworkResponse = null;
    }

    /* package */ void setNetworkTimeMs(long networkTimeMs) {
        this.mNetworkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs() {
        return mNetworkTimeMs;
    }
}
