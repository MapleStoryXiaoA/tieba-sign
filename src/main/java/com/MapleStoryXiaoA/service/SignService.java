package com.MapleStoryXiaoA.service;

import com.MapleStoryXiaoA.entity.Cookie;
import com.MapleStoryXiaoA.util.Encryption;
import com.MapleStoryXiaoA.util.Request;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignService {
	//获取用户所有关注贴吧
	private final String LIKE_URL = "https://tieba.baidu.com/mo/q/newmoindex";
	//贴吧签到接口
	private final String SIGN_URL = "http://c.tieba.baidu.com/c/c/forum/sign";
	private final Request request;

	public SignService(Cookie cookie) {
		this.request = new Request(cookie);
	}

	public void start() {
		//获取用户的tbs
		String tBSURL = "http://tieba.baidu.com/dc/common/tbs";
		request.getSync(tBSURL).map(response -> response.bodyAsJsonObject().getString("tbs"))
				.map(tbs -> request.getSync(LIKE_URL)
						.map(response -> response.bodyAsJsonObject().getJsonObject("data").getJsonArray("like_forum"))
						.flattenAsFlowable(x -> x)
						.filter(obj -> 0 == (((JsonObject) obj).getInteger("is_sign")))
						.map(obj -> ((JsonObject) obj).getString("forum_name"))
						.map(kw -> {
							final MultiMap params = MultiMap.caseInsensitiveMultiMap();
							params.set("kw", kw);
							params.set("sign", Encryption.enCodeMd5("kw=" + kw + "tbs=" + tbs + "tiebaclient!!!"));
							return request.postAsync(SIGN_URL, params)
									.map(resp -> {
										JsonObject jsonObject = resp.bodyAsJsonObject();
										String errorCode = jsonObject.getString("error_code");
										if ("0".equals(errorCode)) {
											Integer signBonusPoint = jsonObject.getJsonObject("user_info").getInteger("sign_bonus_point");
											Integer userSignRank = jsonObject.getJsonObject("user_info").getInteger("user_sign_rank");
											log.info("{}吧签到完成，经验值加{}，你是今天第{}个签到的", kw, signBonusPoint, userSignRank);
											return true;
										} else {
											log.warn("{}吧签到失败,失败原因为【{}】", kw, jsonObject.getString("error_msg"));
											return false;
										}
									});
						}))
				.subscribe();

	}
}
