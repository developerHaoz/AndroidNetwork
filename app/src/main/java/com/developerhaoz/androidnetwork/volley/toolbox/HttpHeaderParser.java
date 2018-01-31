package com.developerhaoz.androidnetwork.volley.toolbox;

import android.annotation.TargetApi;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.developerhaoz.androidnetwork.volley.Cache;
import com.developerhaoz.androidnetwork.volley.Header;
import com.developerhaoz.androidnetwork.volley.NetworkResponse;
import com.developerhaoz.androidnetwork.volley.VolleyLog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 解析 HTTP headers 的有效方法
 *
 * @author Haoz
 * @date 2018/1/30.
 */
public class HttpHeaderParser {

    static final String HEADER_CONTENT_TYPE = "Content_Type";

    private static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";

    private static final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public static Cache.Entry parseCacheHeaders(NetworkResponse response){
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;

        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if(headerValue != null){
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if(headerValue != null){
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if(token.equals("no-cache") || token.equals("no-store")){
                    return null;
                }else if(token.startsWith("max-age=")){
                    try{
                        maxAge = Long.parseLong(token.substring(8));
                    }catch (Exception e){
                    }
                }else if(token.startsWith("stale-while-revalidate=")){
                    try{
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    }catch (Exception e){
                    }
                }else if(token.equals("must-revalidate") || token.equals("proxy-revalidate")){
                    mustRevalidate = true;
                }
            }
        }

        headerValue = headers.get("Expires");
        if(headerValue != null){
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Last-Modified");
        if(headerValue != null){
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        if(hasCacheControl){
            softExpire = now + maxAge * 1000;
            finalExpire = mustRevalidate
                    ? softExpire
                    : softExpire + staleWhileRevalidate * 1000;
        }else if(serverDate > 0 && serverExpires >= serverDate){
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified;
        entry.responseHeaders = headers;
        entry.allResponseHeaders = response.allHeaders;

        return entry;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static long parseDateAsEpoch(String dateStr){
        try {
            return newRfcll23Formatter().parse(dateStr).getTime();
        } catch (ParseException e) {
            VolleyLog.e(e, "Unable to parse dateStr: %s, falling back to 0", dateStr);
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static String formatEpochAsRfc1123(long epoch){
        return newRfcll23Formatter().format(new Date(epoch));
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static SimpleDateFormat newRfcll23Formatter(){
        SimpleDateFormat formatter =
                new SimpleDateFormat(RFC1123_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    }

    public static String parseCharset(Map<String, String> headers, String defaultCharset){
        String contentType = headers.get(HEADER_CONTENT_TYPE);
        if(contentType != null){
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if(pair.length == 2){
                    if(pair[0].equals("charset")){
                        return pair[1];
                    }
                }
            }
        }
        return defaultCharset;
    }

    public static String parseCharset(Map<String, String> headers){
        return parseCharset(headers, DEFAULT_CONTENT_CHARSET);
    }

    static Map<String, String> toHeaderMap(List<Header> allHeaders){
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Header header : allHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    static List<Header> toAllHeaderList(Map<String, String> headers){
        List<Header> allHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            allHeaders.add(new Header(entry.getKey(), entry.getValue()));
        }
        return allHeaders;
    }
}





























