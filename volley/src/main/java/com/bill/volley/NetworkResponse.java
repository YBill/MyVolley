package com.bill.volley;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Bill on 2019/6/5.
 */
public class NetworkResponse {

    public NetworkResponse(byte[] data, Map<String, String> headers) {
        this(
                HttpURLConnection.HTTP_OK,
                data,
                headers);
    }

    public NetworkResponse(
            int statusCode,
            byte[] data,
            Map<String, String> headers) {
        this(statusCode, data, headers, toAllHeaderList(headers));
    }

    public NetworkResponse(
            int statusCode,
            byte[] data,
            List<Header> allHeaders) {
        this(statusCode, data, toHeaderMap(allHeaders), allHeaders);
    }

    private NetworkResponse(
            int statusCode,
            byte[] data,
            Map<String, String> headers,
            List<Header> allHeaders) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        if (allHeaders == null) {
            this.allHeaders = null;
        } else {
            this.allHeaders = Collections.unmodifiableList(allHeaders);
        }
    }

    /**
     * The HTTP status code.
     */
    public final int statusCode;

    /**
     * Raw data from this response.
     */
    public final byte[] data;

    /**
     * Response headers.
     **/
    public final Map<String, String> headers;

    /**
     * All response headers. Must not be mutated directly.
     */
    public final List<Header> allHeaders;


    private static Map<String, String> toHeaderMap(List<Header> allHeaders) {
        if (allHeaders == null) {
            return null;
        }
        if (allHeaders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // Later elements in the list take precedence.
        for (Header header : allHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    private static List<Header> toAllHeaderList(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        if (headers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Header> allHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String, String> header : headers.entrySet()) {
            allHeaders.add(new Header(header.getKey(), header.getValue()));
        }
        return allHeaders;
    }

}
