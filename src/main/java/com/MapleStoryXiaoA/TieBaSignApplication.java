package com.MapleStoryXiaoA;

import com.MapleStoryXiaoA.entity.Cookie;
import com.MapleStoryXiaoA.service.SignService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TieBaSignApplication {

	public static void main(String[] args) {
		if (args.length < 1 || args[0].length() < 1) {
			log.error("请在Secrets中填写BDUSS");
			return;
		}
		//设置cookie
		Cookie cookie = Cookie.getInstance();
		cookie.setBDUSS(args[0]);

		SignService signService = new SignService(cookie);
		signService.start();
	}

}
