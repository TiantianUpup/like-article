package com.h2t.study.service.impl;

import com.h2t.study.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis服务类
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/29 16:05
 */
public class RedisServiceImpl implements RedisService {
    private RedisTemplate redisTemplate;


    /**
     * 用户点赞某篇文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    @Override
    public Long likeArticle(Long userId, Long articleId) {
        if (null == userId || null == articleId) {

        }

        String.valueOf(userId);
        return redisTemplate.opsForSet().add(userId, articleId);
    }
}
