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
@RequestMapping("/api/article")
public class ArticleLikeController {
    @Resource
    private RedisService redisService;

    @PostMapping("/{articleId}/{likedUserId}/{likedPoseId}")
    public Object likeArticle(@PathVariable("articleId") Long articleId,
                              @PathVariable("likedUserId") Long likedUserId,
                              @PathVariable("likedPoseId") Long likedPoseId) {
        return redisService.likeArticle(articleId, likedUserId, likedPoseId);
    }

    @GetMapping("/{userId}")
    public Object getUserLikeArticleIds(@PathVariable("userId") Long userId) {
        return redisService.getUserLikeArticleIds(userId);
    }

    @GetMapping("/total/{userId}/count")
    public Object countUserLike(@PathVariable Long userId) {
        return redisService.countUserLike(userId);
    }
}
