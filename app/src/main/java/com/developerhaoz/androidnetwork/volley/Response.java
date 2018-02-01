package com.developerhaoz.androidnetwork.volley;

/**
 * 封装一个解析后的结果以便分发
 *
 * @author Haoz
 * @date 2018/1/31.
 */
public class Response<T> {

    public interface Listener<T>{
        void onResponse(T response);
    }

    public interface ErrorListener{
        void onErrorResponse(VolleyError error);
    }

    public static <T> Response<T> success(T result, Cache.Entry cacheEntry){
        return new Response<T>(result, cacheEntry);
    }

    public static <T> Response<T> error(VolleyError error){
        return new Response<T>(error);
    }

    public final T result;

    public final Cache.Entry cacheEntry;

    public final VolleyError error;

    public boolean intermediate = false;

    public boolean isSuccess(){
        return error == null;
    }

    private Response(T result, Cache.Entry cacheEntry){
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }

    private Response(VolleyError error){
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
    }
}





















