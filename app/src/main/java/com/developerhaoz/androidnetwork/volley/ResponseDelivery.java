package com.developerhaoz.androidnetwork.volley;

/**
 * @author Haoz
 * @date 2018/2/1.
 */
public interface ResponseDelivery {

    void postResponse(Request<?> request, Response<?> response);

    void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    void postError(Request<?> request, VolleyError error);
}















