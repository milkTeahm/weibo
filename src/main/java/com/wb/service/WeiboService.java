package com.wb.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class WeiboService {

    public static String imageUrl = "https://login.sina.com.cn/sso/qrcode/image";

    public static String checkUrl = "https://login.sina.com.cn/sso/qrcode/check";

    public static String loginUrl = "https://login.sina.com.cn/sso/login.php";

    public static Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");

    public JSONObject getImage() {
        JSONObject data = null;
        // 获取二维码
        Map<String, Object> params = new HashMap<>();
        params.put("entry", "weibo");
        params.put("size", "180");
        params.put("callback", "STK_" + System.currentTimeMillis());
        String imageStr = HttpRequest.get(imageUrl)
                .header(Header.REFERER, "https://weibo.com/")
                .header(Header.HOST, "login.sina.com.cn")
                .form(params)
                .timeout(20000)
                .execute().body();
        // 截取括号里的内容
        Matcher matcher = pattern.matcher(imageStr);
        while(matcher.find()) {
            imageStr = matcher.group();
        }
        log.info("获取二维码---》" + imageStr);
        JSONObject imageJson = JSONUtil.parseObj(imageStr);
        if(imageJson.containsKey("msg") && imageJson.getStr("msg").equals("succ")) {
            data = imageJson.getJSONObject("data");
        }
        return data;
    }

    public JSONObject checkQRByTime(String qrid) {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject checkResult = check(qrid);
                    if(checkResult.containsKey("retcode") && checkResult.getInt("retcode") == 50114002) {
                        // 已扫描未确认状态
                        log.info("二维码状态---》" + checkResult.getStr("msg"));
                    }else if(checkResult.containsKey("retcode") && checkResult.getInt("retcode") == 50114004) {
                        // 失效
                        log.info("二维码状态---》" + checkResult.getStr("msg"));
                    }else if(checkResult.containsKey("retcode") && checkResult.getInt("retcode") == 20000000) {
                        // 已确认状态，登录成功
                        log.info("二维码状态---》" + checkResult.getStr("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

    public JSONObject check(String qrid) {
        JSONObject data = null;
        // 获取二维码
        Map<String, Object> params = new HashMap<>();
        params.put("entry", "weibo");
        params.put("qrid", qrid);
        params.put("callback", "STK_" + System.currentTimeMillis());
        String imageStr = HttpRequest.get(checkUrl)
                .header(Header.REFERER, "https://weibo.com/")
                .header(Header.HOST, "login.sina.com.cn")
                .form(params)
                .timeout(20000)
                .execute().body();
        // 截取括号里的内容
        Matcher matcher = pattern.matcher(imageStr);
        while(matcher.find()) {
            imageStr = matcher.group();
        }
        log.info("检查二维码状态---》" + imageStr);
        JSONObject imageJson = JSONUtil.parseObj(imageStr);
        if(imageJson.containsKey("data") && imageJson.get("data").equals(JSONNull.NULL)) {
            imageJson.remove("data");
        }
        if(imageJson.containsKey("retcode") && imageJson.getInt("retcode") == 50114001) {
            // 未扫描状态
            log.info("二维码状态---》" + imageJson.getStr("msg"));
            // 2秒判断一次
            //Thread.sleep(2000);
            // check(qrid);
        }/*else if(imageJson.containsKey("retcode") && imageJson.getInt("retcode") == 50114002) {
            // 已扫描未确认状态
            log.info("二维码状态---》" + imageJson.getStr("msg"));
        }else if(imageJson.containsKey("retcode") && imageJson.getInt("retcode") == 50114004) {
            // 失效
            log.info("二维码状态---》" + imageJson.getStr("msg"));
        }else if(imageJson.containsKey("retcode") && imageJson.getInt("retcode") == 20000000) {
            // 已确认状态，登录成功
            log.info("二维码状态---》" + imageJson.getStr("msg"));
        }*/
        return imageJson;
    }

    public JSONObject getCookie(String alt) {
        JSONObject data = null;
        // 获取二维码
        Map<String, Object> params = new HashMap<>();
        params.put("entry", "sinawap");
        params.put("returntype", "TEXT");
        params.put("crossdomain", 1);
        params.put("cdult", 3);
        params.put("domain", "weibo.com");
        params.put("alt", alt);
        params.put("savestate", 30);
        params.put("callback", "STK_" + System.currentTimeMillis());
        HttpResponse loginResponse = HttpRequest.get(loginUrl)
                .header(Header.REFERER, "https://weibo.com/")
                .header(Header.HOST, "login.sina.com.cn")
                .form(params)
                .timeout(20000)
                .execute();
        List<String> setCookies = loginResponse.headerList("Set-Cookie");
        setCookies.stream().forEach(setCookie -> {
            log.info("cookie信息--》" + setCookie);
        });
        String loginStr = loginResponse.body();
        // 截取括号里的内容
        Matcher matcher = pattern.matcher(loginStr);
        while(matcher.find()) {
            loginStr = matcher.group();
        }
        log.info("登录---》" + loginStr);
        JSONObject loginJson = JSONUtil.parseObj(loginStr);
        if(loginJson.containsKey("retcode") && loginJson.getInt("retcode") == 0) {
            // 登录成功
            log.info("登录成功---》" + loginJson.getStr("nick"));
            JSONObject loginResult = login(loginJson.getJSONArray("crossDomainUrlList").getStr(0));
            loginJson.putOpt("cookies", loginResult.get("cookies"));
        }
        return loginJson;
    }

    public JSONObject login(String url) {
        JSONObject data = null;
        // 获取二维码
        Map<String, Object> params = new HashMap<>();
        params.put("callback", "STK_" + System.currentTimeMillis());
        HttpResponse loginResponse = HttpRequest.get(url)
                .header(Header.REFERER, "https://weibo.com/")
                .header(Header.HOST, "passport.weibo.com")
                .form(params)
                .timeout(20000)
                .execute();
        List<String> setCookies = loginResponse.headerList("Set-Cookie");
        setCookies.stream().forEach(setCookie -> {
            log.info("最终cookie信息--》" + setCookie);
        });
        String loginStr = loginResponse.body();
        // 截取括号里的内容
        Matcher matcher = pattern.matcher(loginStr);
        while(matcher.find()) {
            loginStr = matcher.group();
        }
        log.info("最终登录---》" + loginStr);
        JSONObject loginJson = JSONUtil.parseObj(loginStr);
        loginJson.putOpt("cookies", setCookies);
        return loginJson;
    }
}
