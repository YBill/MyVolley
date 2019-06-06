package com.bill.volley;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 缓存线程，内部一个死循环阻塞接收，只到调用了 {@link RequestQueue} 的 stop 结束
 */
public class CacheDispatcher extends Thread {

    private final BlockingQueue<Request<?>> mCacheQueue;

    private final BlockingQueue<Request<?>> mNetworkQueue;

    private final Cache mCache;

    private final ResponseDelivery mDelivery;

    private volatile boolean mQuit = false;

    public CacheDispatcher(
            BlockingQueue<Request<?>> cacheQueue,
            BlockingQueue<Request<?>> networkQueue,
            Cache cache,
            ResponseDelivery delivery) {
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
        mCache = cache;
        mDelivery = delivery;
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        mCache.initialize();

        while (true) {
            try {
                processRequest();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void processRequest() throws InterruptedException {
        // Get a request from the cache triage queue, blocking until
        // at least one is available.
        final Request<?> request = mCacheQueue.take();
        processRequest(request);
    }

    void processRequest(final Request<?> request) throws InterruptedException {

        // If the request has been canceled, don't bother dispatching it.
        if (request.isCanceled()) {
            request.finish();
            return;
        }

        Cache.Entry entry = mCache.get(request.getCacheKey());
        if (entry == null) {
            mNetworkQueue.put(request);
            return;
        }

        if (entry.isExpired()) {
            Log.e("Bill", "请求已经过期");
            request.setCacheEntry(entry);
            mNetworkQueue.put(request);
            return;
        }

        Response<?> response =
                request.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));
        mDelivery.postResponse(request, response);

    }

}
