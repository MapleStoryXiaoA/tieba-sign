package com.MapleStoryXiaoA.util;

import com.MapleStoryXiaoA.entity.Cookie;

import io.reactivex.Single;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangzicheng-PC
 */
@Slf4j
public class Request {

	private final Cookie cookie;
	private static final Vertx VERTX;
	private static final WebClient CLIENT;
	private static final MultiMap HEADERS = MultiMap.caseInsensitiveMultiMap();

	static {
		VERTX = Vertx.vertx();
		CLIENT = WebClient.create(VERTX);
		HEADERS.add("connection", "keep-alive");
		HEADERS.add("Content-Type", "application/x-www-form-urlencoded");
		HEADERS.add("charset", "UTF-8");
		HEADERS.add("User-Agent", "bdtb for Android 8.4.0.1");
		HEADERS.add("net", "3");
	}

	public Request(Cookie cookie) {
		this.cookie = cookie;
	}

	public Single<HttpResponse<Buffer>> getSync(String url) {
		return CLIENT.postAbs(url).putHeaders(HEADERS).putHeader("Cookie", cookie.getCookie())
				.rxSend();
	}

	public Single<HttpResponse<Buffer>> postAsync(String url, MultiMap params) {
		final HttpRequest<Buffer> bufferHttpRequest = CLIENT.postAbs(url).putHeaders(HEADERS);
		params.forEach(entry -> bufferHttpRequest.addQueryParam(entry.getKey(), entry.getValue()));
		return bufferHttpRequest.putHeader("Cookie", cookie.getCookie())
				.rxSend();
	}

}
