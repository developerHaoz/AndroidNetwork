package com.developerhaoz.androidnetwork.volley.toolbox;

import com.developerhaoz.androidnetwork.volley.AuthFailureError;
import com.developerhaoz.androidnetwork.volley.Request;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * 基于 HttpURLConnection 的封装
 *
 * @author Haoz
 * @date 2018/2/2.
 */
public class HurlStack extends BaseHttpStack {

    private static final int HTTP_CONTINUE = 100;

    public interface UrlRewriter {

        String rewriteUrl(String originalUrl);
    }

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public HurlStack(){
        this(null);
    }

    public HurlStack(UrlRewriter urlRewriter){
        this(urlRewriter, null);
    }

    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory){
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if(mUrlRewriter != null){
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if(rewritten == null){
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        URL parsedUrl = new URL(url);
        HttpURLConnection connection =openConnection(parsedUrl, request);
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        return null;
    }

    protected HttpURLConnection createConnection(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
        return connection;
    }

    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException{
        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        if("https".equals(url.getProtocol()) && mSslSocketFactory != null){
            ((HttpsURLConnection)connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

}





















