package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * redis服务实现类
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/29 16:05
 */
@Service
public class RedisServiceImpl implements RedisService {
    private RedisTemplate redisTemplate;

    /**
     * 用户点赞某篇文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long likeArticle(Long userId, Long articleId) {
        if (null == userId || null == articleId) {
            throw new CustomException(ErrorCodeEnum.Param_can_not_null);
        }

        return redisTemplate.opsForSet().add(String.valueOf(articleId), String.valueOf(userId));
    }

    /**
     * 取消点赞
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long unlikeArticle(Long userId, Long articleId) {
        return redisTemplate.opsForSet().remove(String.valueOf(articleId), String.valueOf(userId));
    }

    /**
     * 统计点赞数
     *
     * @param articleId
     * @return
     */
    public Long countArticleLike(Long articleId) {
        return redisTemplate.opsForSet().size(articleId);
    }
}
