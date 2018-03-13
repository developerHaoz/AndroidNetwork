package com.developerhaoz.androidnetwork.okhttptest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Haoz
 * @date 2018/3/8.
 */
public class MyInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
