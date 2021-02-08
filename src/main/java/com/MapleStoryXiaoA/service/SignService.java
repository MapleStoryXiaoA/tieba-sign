package com.MapleStoryXiaoA.service;

import com.MapleStoryXiaoA.util.Encryption;
import com.MapleStoryXiaoA.util.Request;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SignService {
    //获取用户所有关注贴吧
    private final String LIKE_URL = "https://tieba.baidu.com/mo/q/newmoindex";
    //获取用户的tbs
    private final String TBS_URL = "http://tieba.baidu.com/dc/common/tbs";
    //贴吧签到接口
    private final String SIGN_URL = "http://c.tieba.baidu.com/c/c/forum/sign";
    //存储用户所关注的贴吧
    private List<String> followList = new ArrayList<String>();
    //签到成功的贴吧列表
    private List<String> successList = new ArrayList<String>();
    //签到失败的贴吧列表
    private List<String> failList = new ArrayList<String>();
    //还未签到的贴吧列表()
    private ConcurrentHashMap<String, Integer> todoList = new ConcurrentHashMap<>();
    //用户的tbs
    private String tbs;

    public void start() {
        getTbs();
        getFollow();
        runSign();
    }


    /**
     * 进行登录，获得tbs ，签到的时候需要用到这个参数
     */
    public void getTbs() {
        try {
            JsonObject jsonObject = Request.getSync(TBS_URL);
            tbs = jsonObject.getString("tbs");
            log.info("获取tbs为{}", tbs);
        } catch (Exception e) {
            log.error("获取tbs出错", e);
        }
    }

    /**
     * 获取用户所关注的贴吧列表
     */
    public void getFollow() {
        try {
            JsonObject jsonObject = Request.getSync(LIKE_URL);
            JsonArray jsonArray = jsonObject.getJsonObject("data").getJsonArray("like_forum");
            // 获取用户所有关注的贴吧
            for (Object array : jsonArray) {
                followList.add(((JsonObject) array).getString("forum_name"));
                if (0 == (((JsonObject) array).getInteger("is_sign"))) {
                    // 将未签到的贴吧加入到 follow 中，待签到
                    todoList.put(((JsonObject) array).getString("forum_name"), 0);
                } else {
                    // 将已经成功签到的贴吧，加入到 success
                    successList.add(((JsonObject) array).getString("forum_name"));
                }
            }
            log.info("总共关注了{}个贴吧", followList.size());
            log.info("未签到的贴吧:{}", todoList.keySet());
            log.info("已签到的贴吧:{}", successList);
        } catch (Exception e) {
            log.error("获取关注的贴吧列表错误", e);
        }
    }


    /**
     * 开始签到
     */
    public void runSign() {
        Map<String, String> params = new HashMap<>();
        params.put("tbs", tbs);
        // 当执行 5 轮所有贴吧还未签到成功就结束操作
        final int flag = 5;
        try {
            for (; ; ) {
                if (todoList.size() <= 0) {
                    return;
                }
                Iterator<Map.Entry<String, Integer>> iterator = todoList.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Integer> entry = iterator.next();
                    String kw = entry.getKey();
                    params.put("kw", kw);
                    params.put("sign", Encryption.enCodeMd5("kw=" + kw + "tbs=" + tbs + "tiebaclient!!!"));
                    log.info("{}吧签到中", kw);
                    Single<HttpResponse<Buffer>> httpResponseSingle = Request.postAsync(SIGN_URL, params);
                    httpResponseSingle.subscribe(
                            resp -> {
                                JsonObject jsonObject = resp.bodyAsJsonObject();
                                String error_code = jsonObject.getString("error_code");
                                if ("0".equals(error_code)) {
                                    Integer sign_bonus_point = jsonObject.getJsonObject("user_info").getInteger("sign_bonus_point");
                                    Integer user_sign_rank = jsonObject.getJsonObject("user_info").getInteger("user_sign_rank");
                                    log.info("{}吧签到完成，经验值加{}，你是今天第{}个签到的", kw, sign_bonus_point, user_sign_rank);
                                    successList.add(kw);
                                    todoList.remove(kw);
                                } else {
                                    log.warn("{}吧签到失败,失败原因为【{}】", kw, jsonObject.getString("error_msg"));
                                    if (todoList.containsKey(kw)) {
                                        int doneTimes = todoList.get(kw) + 1;
                                        if (doneTimes >= flag) {
                                            log.warn("{}吧签到失败{}次,不再继续签到", kw, flag);
                                            todoList.remove(kw);
                                            failList.add(kw);
                                        } else {
                                            log.warn("{}吧将进入重新尝试签到列表...", kw);
                                            todoList.put(kw, doneTimes);
                                        }
                                    }
                                }
                            }
                    );
                }

                Thread.sleep(1000*10);//签太快百度会报失败
            }
        } catch (Exception e) {
            log.error("签到出错", e);
        } finally {
            log.info("今日签到成功的贴吧有{}", successList);
            log.info("今日签到失败的贴吧有{}", failList);
        }
    }
}
