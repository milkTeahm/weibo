package com.wb.controller;

import cn.hutool.json.JSONObject;
import com.wb.service.WeiboService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class WeiboController {

    WeiboService weiboService;

    /**
     * 获取二维码
     * @return
     */
    @RequestMapping("/getImage")
    public JSONObject getImage() {
        return weiboService.getImage();
    }

    /**
     * 检查二维码状态
     * @return
     */
    @RequestMapping("/check")
    public JSONObject check(@RequestParam("qrid") String qrid) {
        return weiboService.check(qrid);
    }

    /**
     * 登录获取cookie
     * @return
     */
    @RequestMapping("/getCookie")
    public JSONObject getCookie(@RequestParam("alt") String alt) {
        return weiboService.getCookie(alt);
    }
}
