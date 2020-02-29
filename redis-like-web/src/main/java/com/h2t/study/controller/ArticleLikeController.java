package com.h2t.study.controller;

import com.h2t.study.service.ArticleService;
import com.h2t.study.service.RedisService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 点赞文章控制层
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

    @Resource
    private ArticleService articleService;

    /**
     * 点赞文章
     */
    @PostMapping("/{articleId}/{likedUserId}/{likedPoseId}")
    public Object likeArticle(@PathVariable("articleId") Long articleId,
                              @PathVariable("likedUserId") Long likedUserId,
                              @PathVariable("likedPoseId") Long likedPoseId) {
        redisService.likeArticle(articleId, likedUserId, likedPoseId);
        return articleId;
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{articleId}/{likedUserId}/{likedPoseId}")
    public Object unlikeArticle(@PathVariable("articleId") Long articleId,
                                @PathVariable("likedUserId") Long likedUserId,
                                @PathVariable("likedPoseId") Long likedPoseId) {
        redisService.unlikeArticle(articleId, likedUserId, likedPoseId);
        return articleId;
    }

    /**
     * 获取用户点赞的文章
     */
    @GetMapping("/user/{likedPoseId}/like")
    public Object getUserLikeArticle(@PathVariable("likedPoseId") Long likedPoseId) {
        return articleService.selectByIds(redisService.getUserLikeArticleIds(likedPoseId));
    }

    /**
     * 统计用户总的文章点赞数
     */
    @GetMapping("/total/user/{likedUserId}")
    public Object countUserLike(@PathVariable("likedUserId") Long likedUserId) {
        return redisService.countUserLike(likedUserId);
    }

    /**
     * 统计某篇文章总点赞数
     */
    @GetMapping("/total/article/{articleId}")
    public Object countArticleLike(@PathVariable("articleId") Long articleId) {
        return redisService.countArticleLike(articleId);
    }
}
