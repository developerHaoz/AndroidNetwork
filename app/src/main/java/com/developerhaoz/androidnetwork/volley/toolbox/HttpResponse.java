package com.developerhaoz.androidnetwork.volley.toolbox;

import com.developerhaoz.androidnetwork.volley.Header;

import java.io.InputStream;
import java.util.List;

/**
 * 从 HTTP 服务端返回的内容
 *
 * @author Haoz
 * @date 2018/2/2.
 */
public class HttpResponse {

    private final int mStatusCode;
    private final List<Header> mHeaders;
    private final int mContentLength;
    private final InputStream mContent;

    public HttpResponse(
            int statusCode, List<Header> headers, int contentLength, InputStream content) {
        mStatusCode = statusCode;
        mHeaders = headers;
        mContentLength = contentLength;
        mContent = content;
    }

    public final int getStatusCode() {
        return mStatusCode;
    }

    public final List<Header> getHeaders() {
        return mHeaders;
    }

    public final int getContentLength() {
        return mContentLength;
    }

    public final InputStream getContent() {
        return mContent;
    }
}






















