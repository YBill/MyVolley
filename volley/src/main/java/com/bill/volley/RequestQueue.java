package com.bill.volley;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 定义了和外部交互的方法，start方法默认启动一个缓存线程和4个网络线程，
 * add 添加请求
 * stop结束线程
 * 建议 RequestQueue 全局创建一个，app退出时调用stop结束
 */
public class RequestQueue {

    private final PriorityBlockingQueue<Request<?>> mCacheQueue = new PriorityBlockingQueue<>();

    private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue<>();

    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    private final Cache mCache;

    private final Network mNetwork;

    private final ResponseDelivery mDelivery;

    private final NetworkDispatcher[] mDispatchers;

    private CacheDispatcher mCacheDispatcher;

    private final Set<Request<?>> mCurrentRequests = new HashSet<>();

    public RequestQueue(
            Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        mCache = cache;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
    }

    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
        this(
                cache,
                network,
                threadPoolSize,
                new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(Cache cache, Network network) {
        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }

    public void start() {
        stop(); // Make sure any currently running dispatchers are stopped.
        // Create the cache dispatcher and start it.
        mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
        mCacheDispatcher.start();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher =
                    new NetworkDispatcher(mNetworkQueue, mNetwork, mCache, mDelivery);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    public void stop() {
        if (mCacheDispatcher != null) {
            mCacheDispatcher.quit();
        }
        for (final NetworkDispatcher mDispatcher : mDispatchers) {
            if (mDispatcher != null) {
                mDispatcher.quit();
            }
        }
    }

    public <T> Request<T> add(Request<T> request) {
        // Tag the request as belonging to this queue and add it to the set of current requests.
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }

        // If the request is uncacheable, skip the cache queue and go straight to the network.
        if (!request.shouldCache()) {
            mNetworkQueue.add(request);
            return request;
        }
        mCacheQueue.add(request);
        return request;
    }

    <T> void finish(Request<T> request) {
        // Remove from the set of requests currently being processed.
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
    }

}
