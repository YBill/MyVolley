package com.bill.volley;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 真正的交付实现，内部通过Handler将网络请求转到主线程
 */
public class ExecutorDelivery implements ResponseDelivery {

    private final Executor mResponsePoster;

    public ExecutorDelivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster =
                new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        handler.post(command);
                    }
                };
    }

    public ExecutorDelivery(Executor executor) {
        mResponsePoster = executor;
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response));
    }

    @Override
    public void postError(Request<?> request, VolleyError error) {
        Response<?> response = Response.error(error);
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response));
    }

    private static class ResponseDeliveryRunnable implements Runnable {

        private final Request mRequest;
        private final Response mResponse;

        public ResponseDeliveryRunnable(Request request, Response response) {
            mRequest = request;
            mResponse = response;
        }

        @Override
        public void run() {

            // If this request has canceled, finish it and don't deliver.
            if (mRequest.isCanceled()) {
                mRequest.finish();
                return;
            }

            // Deliver a normal response or error, depending.
            if (mResponse.isSuccess()) {
                mRequest.deliverResponse(mResponse.result);
            } else {
                mRequest.deliverError(mResponse.error);
            }

            if (!mResponse.intermediate) {
                mRequest.finish();
            }

        }
    }
}
