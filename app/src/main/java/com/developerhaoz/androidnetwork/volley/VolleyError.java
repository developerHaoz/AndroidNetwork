package com.developerhaoz.androidnetwork.volley;

/**
 * 封装 Volley 错误的异常样式类
 *
 * @author Haoz
 * @date 2018/1/29.
 */
public class VolleyError extends Exception {

    // TODO：NetworkResponse 类还没写，等写完再补充完整这个类
    public final NetworkResponse networkResponse;
    private long networkTimeMs;

    public VolleyError(){
        networkResponse = null;
    }

    public VolleyError(NetworkResponse response){
        networkResponse = response;
    }

    public VolleyError(String exceptionMessage){
        super(exceptionMessage);
        networkResponse = null;
    }

    public VolleyError(String exceptionMessage, Throwable reason){
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public VolleyError(Throwable cause){
        super(cause);
        networkResponse = null;
    }

    public void setNetworkTimeMs(long networkTimeMs) {
        this.networkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs() {
        return networkTimeMs;
    }
}













