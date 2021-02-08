package com.MapleStoryXiaoA.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存入用户所填写的BDUSS
 *
 * @author MapleStoryXiaoA
 * @date 2020-02-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cookie {

    private static final Cookie cookie = new Cookie();
    public static Cookie getInstance() {
        return cookie;
    }

    private String BDUSS;

    public String getCookie() {
        return "BDUSS=" + this.BDUSS;
    }
}
