package com.developerhaoz.androidnetwork.volley;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * @author Haoz
 * @date 2018/2/1.
 */
public class ExecutorDelivery implements ResponseDelivery {

    private final Executor mResponsePoster;

    public ExecutorDelivery(final Handler handler){
        mResponsePoster = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    public ExecutorDelivery(Executor executor){
        mResponsePoster = executor;
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        postResponse(request, response, null);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
        request.markDelivered();
        request.addMarker("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
    }

    @Override
    public void postError(Request<?> request, VolleyError error) {
        request.addMarker("post-error");
        Response<?> response = Response.error(error);
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, null));
    }

    private class ResponseDeliveryRunnable implements Runnable{
        private final Request mRequest;
        private final Response mResponse;
        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable){
            this.mRequest = request;
            this.mResponse = response;
            this.mRunnable = runnable;
        }

        @Override
        public void run() {
            if(mRequest.isCanceled()){
                mRequest.finish("caceled-at-delivery");
                return;
            }

            if(mResponse.isSuccess()){
                mRequest.deliverResponse(mResponse.result);
            }else {
                mRequest.deliverError(mResponse.error);
            }

            if(mResponse.intermediate){
                mRequest.addMarker("intermediate-response");
            } else {
                mRequest.finish("done");
            }

            if(mRunnable != null){
                mRunnable.run();
            }
        }
    }
}




















