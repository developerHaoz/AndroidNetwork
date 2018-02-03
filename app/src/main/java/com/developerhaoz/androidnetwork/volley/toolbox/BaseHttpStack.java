package com.developerhaoz.androidnetwork.volley.toolbox;

import com.developerhaoz.androidnetwork.volley.AuthFailureError;
import com.developerhaoz.androidnetwork.volley.Request;

import java.io.IOException;
import java.util.Map;

/**
 * @author Haoz
 * @date 2018/2/2.
 */
public abstract class BaseHttpStack {

    public abstract HttpResponse executeRequest(
            Request<?> request, Map<String, String> additionalHeaders)
        throws IOException, AuthFailureError;
}























