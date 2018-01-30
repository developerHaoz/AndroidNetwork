package com.developerhaoz.androidnetwork.volley;

/**
 * @author Haoz
 * @date 2018/1/29.
 */
public class ServerError extends VolleyError{
    // TODO：待补充
    public ServerError(int networkResponse){
        super(networkResponse);
    }

    public ServerError(){
        super();
    }
}
