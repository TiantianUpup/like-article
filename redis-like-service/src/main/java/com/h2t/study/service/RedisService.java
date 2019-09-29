package com.h2t.study.service;

/**
 * TODO Description
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/29 16:10
 */
public interface RedisService {
    /**
     * 用户点赞某篇文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    Long likeArticle(Long userId, Long articleId);
}
