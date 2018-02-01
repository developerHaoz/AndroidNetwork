package com.developerhaoz.androidnetwork.volley;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 执行网络请求后返回的数据和 headers
 *
 * @author Haoz
 * @date 2018/1/30.
 */
public class NetworkResponse {

    public final int statusCode;

    public final byte[] data;

    public final Map<String, String> headers;

    public final List<Header> allHeaders;

    public final boolean notModified;

    public final long networkTimeMs;

    /**
     * @deprecated 这个构造器不能处理那些从服务器返回的包含多个名称相同 Headers 的 responses
     */
    @Deprecated
    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers,
                           boolean notModified, long networkTimeMs){
        this(statusCode, data, headers, toAllHeaderList(headers), notModified, networkTimeMs);
    }

    public NetworkResponse(byte[] data){
        this(HttpURLConnection.HTTP_OK, data, false, 0, Collections.<Header>emptyList());
    }

    public NetworkResponse(byte[] data, Map<String, String> headers) {
        this(HttpURLConnection.HTTP_OK, data, headers, false, 0);
    }

    public NetworkResponse(int statusCode, byte[] data, boolean notModified, long networkTimeMs,
                           List<Header> allHeaders){
        this(statusCode, data,toHeaderMap(allHeaders), allHeaders, notModified, networkTimeMs);
    }

    private NetworkResponse(int statusCode, byte[] data, Map<String, String> headers,
                            List<Header> allHeaders, boolean notModified, long networkTimeMs){
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        if(allHeaders == null){
            this.allHeaders = null;
        }else {
            this.allHeaders = Collections.unmodifiableList(allHeaders);
        }
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
    }

    private static Map<String, String> toHeaderMap(List<Header> allHeaders){
        if(allHeaders == null){
            return null;
        }

        if(allHeaders.isEmpty()){
            return Collections.emptyMap();
        }

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Header header : allHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        return  headers;
    }

    private static List<Header> toAllHeaderList(Map<String, String> headers){
        if(headers == null){
            return null;
        }

        if(headers.isEmpty()){
            return Collections.emptyList();
        }

        List<Header> allHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            allHeaders.add(new Header(entry.getKey(), entry.getValue()));
        }
        return allHeaders;
    }


}





















