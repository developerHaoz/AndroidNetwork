package com.developerhaoz.androidnetwork.volley;

/**
 * 请求的重试策略
 *
 * @author Haoz
 * @date 2018/1/31.
 */
public interface RetryPolicy {

    int getCurrentTimeout();

    int getCurrentRetryCount();

    void retry(VolleyError error) throws VolleyError;
}




















