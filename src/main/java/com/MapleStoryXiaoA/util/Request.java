package com.MapleStoryXiaoA.util;

import com.MapleStoryXiaoA.entity.Cookie;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class Request {

    private static final Cookie cookie = Cookie.getInstance();

    public static JsonObject getSync(String url) throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);
//        log.info("webClient正在请求{}", url);

        final CompletableFuture<JsonObject> jsonObject = new CompletableFuture<>();

        HttpRequest<Buffer> request = client.getAbs(url);
        MultiMap headers = request.headers();
        headers.clear();
        headers.add("connection", "keep-alive");
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("charset", "UTF-8");
        headers.add("User-Agent", "bdtb for Android 8.4.0.1");
        headers.add("net", "3");
        headers.add("Cookie", cookie.getCookie());
//        headers.add("Accept-Encoding", "gzip");

        request.send(ar -> {
            if (ar.succeeded()) {
                // 获取响应
                HttpResponse<Buffer> response = ar.result();
//                log.info("webClient收到{}响应状态码为{},响应体为{}", url, response.statusCode(), response.bodyAsString("utf-8"));
                jsonObject.complete(response.bodyAsJsonObject());
            } else {
                log.error("webClient get请求出错{}", ar.cause().getMessage());
            }
        });
        return jsonObject.get();
    }

    public static Single<HttpResponse<Buffer>> postAsync(String url, Map<String, String> params) {
        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);
//        log.info("webClient正在请求{}", url);

        HttpRequest<Buffer> bufferHttpRequest = client.postAbs(url);
        MultiMap headers = bufferHttpRequest.headers();
        headers.clear();
        headers.add("connection", "keep-alive");
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("charset", "UTF-8");
        headers.add("User-Agent", "bdtb for Android 8.4.0.1");
        headers.add("net", "3");
        headers.add("Cookie", cookie.getCookie());
        bufferHttpRequest.queryParams().addAll(params);
        return bufferHttpRequest.rxSend();
    }

}
