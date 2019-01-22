package com.yqc.netty.server.http;

import lombok.Data;

/**
 * @author yangqc
 */
@Data
public class HttpRequest {

    private final HttpRequestBody body;

    private final HttpRequestHeader header;

    public HttpRequest(HttpRequestHeader httpRequestHeader, HttpRequestBody httpRequestBody) {
        this.header = httpRequestHeader;
        this.body = httpRequestBody;
    }
}
