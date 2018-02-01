package com.developerhaoz.androidnetwork.volley;

/**
 * 默认的请求重试策略
 *
 * @author Haoz
 * @date 2018/1/31.
 */
public class DefaultRetryPolicy implements RetryPolicy {

    private int mCurrentTimeoutMs;

    private int mCurrentRetryCount;

    private final int mMaxNumRetries;

    private final float mBackoffMultiplier;

    public static final int DEFAULT_TIMEOUT_MS = 2500;

    public static final int DEFAULT_MAX_RETRIES = 1;

    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public DefaultRetryPolicy(){
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }

    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier){
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }

    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }

    public float getBackoffMultiplier() {
        return mBackoffMultiplier;
    }

    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        mCurrentRetryCount++;
        mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
        if(!hasAttemptRemaining()){
            throw error;
        }
    }

    protected  boolean hasAttemptRemaining(){
        return mCurrentRetryCount <= mMaxNumRetries;
    }
}


















