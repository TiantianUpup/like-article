package com.h2t.study.service;

import java.util.List;

/**
 * redis服务接口
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/29 16:10
 */
public interface RedisService {
    /**
     * 用户点赞某篇文章
     *
     * @param likedUserId 被点赞用户ID
     * @param likedPostId 点赞用户
     * @param articleId   文章ID
     */
    void likeArticle(Long articleId, Long likedUserId, Long likedPostId);

    /**
     * 取消点赞
     *
     * @param likedUserId 被点赞用户ID
     * @param likedPostId 点赞用户
     * @param articleId   文章ID
     */
    void unlikeArticle(Long articleId, Long likedUserId, Long likedPostId);

    /**
     * 统计某篇文章总点赞数
     *
     * @param articleId
     * @return
     */
    Long countArticleLike(Long articleId);

    /**
     * 统计用户总的文章点赞数
     *
     * @param likedUserId
     * @return
     */
    Long countUserLike(Long likedUserId);

    /**
     * 获取用户点赞的文章
     *
     * @param likedPostId 点赞用户ID
     * @return
     */
    List<Long> getUserLikeArticleIds(Long likedPostId);
}
