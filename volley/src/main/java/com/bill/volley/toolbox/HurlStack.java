package com.bill.volley.toolbox;

import com.bill.volley.Header;
import com.bill.volley.Request;
import com.bill.volley.VolleyError;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bill on 2019/6/5.
 */

/**
 * 真正的网络请求，内部通过 {@link HttpURLConnection} 实现
 */
public class HurlStack implements HttpStack {

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, VolleyError {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<>();
        map.putAll(additionalHeaders);
        map.putAll(request.getHeaders());
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);

        for (String headerName : map.keySet()) {
            connection.setRequestProperty(headerName, map.get(headerName));
        }
        setConnectionParametersForRequest(connection, request);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        StatusLine statusLine =
                new BasicStatusLine(
                        protocolVersion, responseCode, /* reasonPhrase= */ "");
        BasicHttpResponse apacheResponse = new BasicHttpResponse(statusLine);

        List<org.apache.http.Header> headers = new ArrayList<>();
        List<Header> headerList = Collections.unmodifiableList(convertHeaders(connection.getHeaderFields()));
        for (Header header : headerList) {
            headers.add(new BasicHeader(header.getName(), header.getValue()));
        }
        apacheResponse.setHeaders(headers.toArray(new org.apache.http.Header[headers.size()]));

        InputStream responseStream = connection.getInputStream();
        if (responseStream != null) {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(responseStream);
            entity.setContentLength(connection.getContentLength());
            apacheResponse.setEntity(entity);
        }

        return apacheResponse;

    }

    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        return connection;
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());

        return connection;
    }

    static void setConnectionParametersForRequest(
            HttpURLConnection connection, Request<?> request) throws IOException, VolleyError {
        switch (request.getMethod()) {
            case Request.Method.GET:
                connection.setRequestMethod("GET");
                break;
            case Request.Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException, VolleyError {
        byte[] body = request.getBody();
        if (body != null) {
            addBody(connection, request, body);
        }
    }

    private static void addBody(HttpURLConnection connection, Request<?> request, byte[] body)
            throws IOException {
        connection.setDoOutput(true);
        if (!connection.getRequestProperties().containsKey(HttpHeaderParser.HEADER_CONTENT_TYPE)) {
            connection.setRequestProperty(
                    HttpHeaderParser.HEADER_CONTENT_TYPE, request.getBodyContentType());
        }
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(body);
        out.close();
    }

    static List<Header> convertHeaders(Map<String, List<String>> responseHeaders) {
        List<Header> headerList = new ArrayList<>(responseHeaders.size());
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            if (entry.getKey() != null) {
                for (String value : entry.getValue()) {
                    headerList.add(new Header(entry.getKey(), value));
                }
            }
        }
        return headerList;
    }

}
