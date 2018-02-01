package com.developerhaoz.androidnetwork.volley;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * 所有网络请求的基类
 *
 * @author Haoz
 * @date 2018/1/31.
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTION = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    interface NetworkRequestCompleteListener {

        void onResponseReceived(Request<?> request, Response<?> response);

        void onNoUsableResponseReceived(Request<?> request);
    }

    private final VolleyLog.MarkerLog mEventLog = VolleyLog.MarkerLog.ENABLED ? new VolleyLog.MarkerLog() : null;

    private final int mMethod;

    private final String mUrl;

    private final int mDefaultTrafficStatsTag;

    private final Object mLock = new Object();

    private Response.ErrorListener mErrorListener;

    private Integer mSequence;

    private RequestQueue mRequestQueue;

    private boolean mShouldCache = true;

    private boolean mCanceled = false;

    private boolean mResponseDelivered = false;

    private boolean mShouldRetryServerErrors = false;

    private RetryPolicy mRetryPolicy;

    private Cache.Entry mCacheEntry = null;

    private Object mTag;

    private NetworkRequestCompleteListener mRequestCompleteListener;

    public Request(int method, String url, Response.ErrorListener listener) {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public int getMethod() {
        return mMethod;
    }

    public Request<?> setTag(Object tag) {
        mTag = tag;
        return this;
    }

    public Object getTag() {
        return mTag;
    }

    public Response.ErrorListener getErrorListener() {
        return mErrorListener;
    }

    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        this.mRetryPolicy = retryPolicy;
        return this;
    }

    public void addMarker(String tag) {
        if (VolleyLog.MarkerLog.ENABLED) {
            mEventLog.add(tag, Thread.currentThread().getId());
        }
    }

    // TODO: 就差这个了
    void finish(final String tag){
        if(mRequestQueue != null){
        }
    }


    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    public final Request<?> setSequence(Integer sequence) {
        mSequence = sequence;
        return this;
    }

    public Integer getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCacheKey() {
        return getUrl();
    }

    public Request<?> setCacheEntry(Cache.Entry cacheEntry) {
        mCacheEntry = cacheEntry;
        return this;
    }

    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    public void cancel() {
        synchronized (mLock) {
            mCanceled = true;
            mErrorListener = null;
        }
    }

    public boolean isCanceled() {
        synchronized (mLock) {
            return mCanceled;
        }
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }

    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, e);
        }
    }

    public final Request<?> setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }

    public final boolean shouldCache() {
        return mShouldCache;
    }

    public final Request<?> setShouldRetryServerErrors(boolean shouldRetryServerErrors) {
        mShouldRetryServerErrors = shouldRetryServerErrors;
        return this;
    }

    public final boolean shouldRetryServerErrors() {
        return mShouldRetryServerErrors;
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

    public final int getTimeoutMs() {
        return mRetryPolicy.getCurrentTimeout();
    }

    public RetryPolicy getRetryPolicy() {
        return mRetryPolicy;
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

    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    protected VolleyError parseNetworkError(VolleyError volley) {
        return volley;
    }

    protected abstract void deliverResponse(T response);

    public void deliverError(VolleyError error) {
        Response.ErrorListener listener;
    }

    void setNetworkRequestCompleteListener(
            NetworkRequestCompleteListener requestCompleteListener) {
        synchronized (mLock) {
            this.mRequestCompleteListener = requestCompleteListener;
        }
    }

    /**
     * TODO: 还是不太懂它这个回调到底是在哪回调的。
     */
    void notifyListenerResponseReceived(Response<?> response) {
        NetworkRequestCompleteListener listener;
        synchronized (mLock){
            listener = mRequestCompleteListener;
        }
        if(listener != null){
            listener.onResponseReceived(this, response);
        }
    }

    void notifyListenerResponseNotUsable(){
        NetworkRequestCompleteListener listener;
        synchronized (mLock){
            listener = mRequestCompleteListener;
        }
        if(listener != null){
            listener.onNoUsableResponseReceived(this);
        }
    }

    @Override
    public int compareTo(@NonNull Request<T> o) {
        Priority left = this.getPriority();
        Priority right = o.getPriority();

        return left == right ?
                this.mSequence - o.mSequence:
                right.ordinal() - left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " "
                + getPriority() + " " + mSequence;
    }
}





























