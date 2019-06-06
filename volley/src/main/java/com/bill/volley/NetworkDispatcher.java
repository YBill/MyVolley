package com.bill.volley;

import android.support.annotation.VisibleForTesting;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 网络线程，内部一个死循环阻塞接收，只到调用了 {@link RequestQueue} 的 stop 结束
 * 有请求到来则调用 {@link Network} 请求数据
 */
public class NetworkDispatcher extends Thread {

    private final BlockingQueue<Request<?>> mQueue;

    private final Network mNetwork;

    private final Cache mCache;

    private final ResponseDelivery mDelivery;

    private volatile boolean mQuit = false;

    public NetworkDispatcher(
            BlockingQueue<Request<?>> queue,
            Network network,
            Cache cache,
            ResponseDelivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
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
        // Take a request from the queue.
        Request<?> request = mQueue.take();
        processRequest(request);
    }

    @VisibleForTesting
    void processRequest(Request<?> request) {

        try {
            if (request.isCanceled()) {
                request.finish();
                return;
            }

            NetworkResponse networkResponse = mNetwork.performRequest(request);

            if (request.hasHadResponseDelivered()) {
                request.finish();
                return;
            }

            Response<?> response = request.parseNetworkResponse(networkResponse);

            if (request.shouldCache() && response.cacheEntry != null) {
                mCache.put(request.getCacheKey(), response.cacheEntry);
            }

            // Post the response back.
            request.markDelivered();
            mDelivery.postResponse(request, response);

        } catch (VolleyError error) {
            error.printStackTrace();
            error = request.parseNetworkError(error);
            mDelivery.postError(request, error);
        } catch (Exception e) {
            VolleyError volleyError = new VolleyError(e);
            mDelivery.postError(request, volleyError);
        }


    }

}
