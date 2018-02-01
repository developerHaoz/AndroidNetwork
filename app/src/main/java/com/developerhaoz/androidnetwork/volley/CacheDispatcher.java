package com.developerhaoz.androidnetwork.volley;

import android.os.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 一个用于执行缓存队列请求的线程
 *
 * @author Haoz
 * @date 2018/2/1.
 */
public class CacheDispatcher extends Thread {

    private static final boolean DEBUG = VolleyLog.DEBUG;

    private final BlockingQueue<Request<?>> mCacheQueue;

    private final BlockingQueue<Request<?>> mNetworkQueue;

    private final Cache mCache;

    private final ResponseDelivery mDelivery;

    private volatile boolean mQuit = false;

    private final WaitingRequestManager mWaitingRequestManager;

    public CacheDispatcher(
            BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue,
            Cache cache, ResponseDelivery delivery) {
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
        mCache = cache;
        mDelivery = delivery;
        mWaitingRequestManager = new WaitingRequestManager(this);
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        if(DEBUG) VolleyLog.v("start new dispatcher");
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mCache.initialize();

        while(true){
            try{
                processRequest();
            }catch (InterruptedException e){
                if(mQuit){
                    return;
                }
            }
        }
    }

    private void processRequest() throws InterruptedException{

        // ① 从缓存队列中取出 Request
        final Request<?> request = mCacheQueue.take();
        request.addMarker("cache-queue-take");

        // ② 判断 Request 是否已经取消
        if(request.isCanceled()){
            request.finish("cache-discard-canceled");
            return;
        }

        Cache.Entry entry = mCache.get(request.getCacheKey());

        // ③ 判断该 Request 是否有进行缓存
        if(entry == null){
            request.addMarker("cache-miss");
            if(!mWaitingRequestManager.maybeAddToWaitingRequests(request)){
                mNetworkQueue.put(request);
            }
            return;
        }

        // ④ 判断缓存是否过期
        if(entry.isExpired()){
            request.addMarker("cache-hit-expired");
            request.setCacheEntry(entry);
            if(!mWaitingRequestManager.maybeAddToWaitingRequests(request)){
                mNetworkQueue.put(request);
            }
            return;
        }

        request.addMarker("cache-hit");
        Response<?> response = request.parseNetworkResponse(
                new NetworkResponse(entry.data, entry.responseHeaders));
        request.addMarker("cache-hit-parsed");

        if(!entry.refreshNeeded()){
            mDelivery.postResponse(request, response);
        } else {
            request.addMarker("cache-hit-refresh-needed");
            request.setCacheEntry(entry);

            response.intermediate = true;

            if(!mWaitingRequestManager.maybeAddToWaitingRequests(request)){
                mDelivery.postResponse(request, response, new Runnable() {
                    @Override
                    public void run() {
                        try{
                            mNetworkQueue.put(request);
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            } else {
              mDelivery.postResponse(request, response);
            }
        }
    }

    private static class WaitingRequestManager implements Request.NetworkRequestCompleteListener {

        private final Map<String, List<Request<?>>> mWaitingRequests = new HashMap<>();

        private final CacheDispatcher mCacheDispatcher;

        WaitingRequestManager(CacheDispatcher cacheDispatcher) {
            mCacheDispatcher = cacheDispatcher;
        }

        @Override
        public void onResponseReceived(Request<?> request, Response<?> response) {
            if (response.cacheEntry == null || response.cacheEntry.isExpired()) {
                onNoUsableResponseReceived(request);
                return;
            }
            String cacheKey = request.getCacheKey();
            List<Request<?>> waitingRequests;
            synchronized (this) {
                waitingRequests = mWaitingRequests.remove(cacheKey);
            }
            if (waitingRequests != null) {
                if (VolleyLog.DEBUG) {
                    VolleyLog.v("Releasing %d waiting requests for cacheKey=%s", waitingRequests.size(), cacheKey);
                }

                for (Request<?> waitingRequest : waitingRequests) {
                    mCacheDispatcher.mDelivery.postResponse(waitingRequest, response);
                }
            }
        }

        @Override
        public synchronized void onNoUsableResponseReceived(Request<?> request) {
            String cacheKey = request.getCacheKey();
            List<Request<?>> waitingRequests = mWaitingRequests.remove(cacheKey);
            if (waitingRequests != null && !waitingRequests.isEmpty()) {
                if (VolleyLog.DEBUG) {
                    VolleyLog.v("%d waiting requests for cacheKey=%s; resend to network",
                            waitingRequests.size(), cacheKey);
                }
                Request<?> nextInLine = waitingRequests.remove(0);
                mWaitingRequests.put(cacheKey, waitingRequests);
                nextInLine.setNetworkRequestCompleteListener(this);
                try {
                    mCacheDispatcher.mNetworkQueue.put(nextInLine);
                } catch (InterruptedException e) {
                    VolleyLog.e("Counldn't add request to queue. %s", e.toString());
                    Thread.currentThread().interrupt();
                    mCacheDispatcher.quit();
                }
            }
        }

        private synchronized boolean maybeAddToWaitingRequests(Request<?> request) {
            String cacheKey = request.getCacheKey();

            if (mWaitingRequests.containsKey(cacheKey)) {
                List<Request<?>> stagedRequests = mWaitingRequests.get(cacheKey);
                if (stagedRequests == null) {
                    stagedRequests = new ArrayList<>();
                }
                request.addMarker("waitring-for-response");
                stagedRequests.add(request);
                mWaitingRequests.put(cacheKey, stagedRequests);
                if (VolleyLog.DEBUG) {
                    VolleyLog.d("Request for cacheKey=%s is in flight, putting on hold.", cacheKey);
                }
                return true;
            } else {
                mWaitingRequests.put(cacheKey, null);
                request.setNetworkRequestCompleteListener(this);
                if (VolleyLog.DEBUG) {
                    VolleyLog.d("new request, sending to network %s", cacheKey);
                }
                return false;
            }
        }
    }

}




























