package com.wb.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static String imageUrl = "https://login.sina.com.cn/sso/qrcode/image";

    public static String gen = "https://v2.qr.weibo.cn/inf/gen";

    public static Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");

    public static void main(String[] args) {
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
        System.out.println(imageStr);
        // 截取括号里的内容
        Matcher matcher = pattern.matcher(imageStr);
        while(matcher.find()) {
            imageStr = matcher.group();
        }
        System.out.println(imageStr);
        JSONObject imageJson = JSONUtil.parseObj(imageStr);
        if(imageJson.containsKey("msg") && imageJson.getStr("msg").equals("succ")) {
            JSONObject data = imageJson.getJSONObject("data");
            // 获取图片地址
            String gen = HttpRequest.get("https:" + data.getStr("image"))
                    .execute().body();
            System.out.println(gen);
        }
    }
}
