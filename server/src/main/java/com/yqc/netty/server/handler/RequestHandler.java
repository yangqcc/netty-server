package com.yqc.netty.server.handler;

import com.yqc.netty.server.http.HttpRequest;
import com.yqc.netty.server.http.HttpResponse;

/**
 * @author yangqc
 */
public interface RequestHandler {

    HttpResponse handle(HttpRequest request);
}
