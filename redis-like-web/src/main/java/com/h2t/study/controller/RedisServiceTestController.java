package com.h2t.study.controller;

import com.h2t.study.service.RedisService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * TODO Description
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/30 15:31
 */
@RestController
@RequestMapping("/api/test")
public class RedisServiceTestController {
    @Resource
    private RedisService redisService;

    @PostMapping("/{articleId}/{userId}")
    public Object likeArticle(@PathVariable("articleId") Long articleId,
                              @PathVariable("userId") Long userId) {
        return redisService.likeArticle(articleId, userId);
    }

    @GetMapping("/test")
    public Object test() {
        return "test";
    }
}
