package com.h2t.study.service;

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
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    Long likeArticle(Long articleId, Long userId);

    /**
     * 取消点赞
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    Long unlikeArticle(Long articleId, Long userId);

    /**
     * 统计点赞数
     *
     * @param articleId
     * @return
     * */
    Long countArticleLike(Long articleId);
}
