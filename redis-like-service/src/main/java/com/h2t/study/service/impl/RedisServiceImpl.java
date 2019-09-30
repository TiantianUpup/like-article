package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis服务实现类
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/09/29 16:05
 */
@Service
public class RedisServiceImpl implements RedisService {
    Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 用户点赞某篇文章 TODO 分布式部署是不是需考虑分布式事务、分布式锁
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long likeArticle(Long articleId, Long userId) {
        validateParam(articleId, userId);

        Long result = 0L;
        //1.用户总点赞数+1
        try {
            redisTemplate.multi();  //开启事务
            if (redisTemplate.opsForValue().get(userId) == null) {
                redisTemplate.opsForValue().set(String.valueOf(userId), "1");
            } else {
                redisTemplate.opsForValue().increment(String.valueOf(userId), 1);

            }
            //2.用户喜欢的文章+1
            redisTemplate.opsForSet().add(String.format("user_%d", userId), String.valueOf(articleId));
            //3.文章点赞数+1
            result = redisTemplate.opsForSet().add(String.valueOf(articleId), String.valueOf(userId));
            redisTemplate.exec();  //执行事务
        } catch (Exception e) {
            logger.error("点赞执行过程中出错将进行回滚，articleId:{}，userId:{}，errorMsg:{}", articleId, userId, e.getMessage());
        }

        return result;
    }

    /**
     * 取消点赞
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long unlikeArticle(Long articleId, Long userId) {
        Long result = 0L;
        try {
            redisTemplate.multi();  //开启事务
            validateParam(articleId, userId);
            //1.用户总点赞数-1
            redisTemplate.opsForValue().decrement(String.valueOf(userId), 1);
            //2.用户喜欢的文章-1
            redisTemplate.opsForSet().remove(String.format("user_%d", userId), String.valueOf(articleId));
            //3.取消用户某篇文章的点赞数
            result = redisTemplate.opsForSet().remove(String.valueOf(articleId), String.valueOf(userId));
            redisTemplate.exec(); //执行命令
        } catch (Exception e) {
            logger.error("取消点赞执行过程中出错将进行回滚，articleId:{}，userId:{}，errorMsg:{}", articleId, userId, e.getMessage());
            redisTemplate.discard();
        }

        return result;
    }

    /**
     * 统计某篇文章总点赞数
     *
     * @param articleId 文章ID
     * @return
     */
    public Long countArticleLike(Long articleId) {
        validateParam(articleId);
        return redisTemplate.opsForSet().size(String.valueOf(articleId));
    }

    /**
     * 统计用户总的文章点赞数
     *
     * @param userId
     * @return
     */
    public Long countUserLike(Long userId) {
        validateParam(userId);
        return redisTemplate.opsForSet().size(String.valueOf(userId));
    }

    /**
     * 获取用户点赞的文章
     *
     * @param userId 用户ID
     * @return
     */
    public List<Long> getUserLikeArticleIds(Long userId) {
        validateParam(userId);
        String userKey = String.format("user_%d", userId);
        Set<String> articleIdSet = redisTemplate.opsForSet().members(userKey);
        return articleIdSet.stream()
                .map(s -> Long.parseLong(s)).collect(Collectors.toList());
    }

    /**
     * 入参验证
     *
     * @param params
     * @throws CustomException
     */
    private void validateParam(Long... params) {
        for (Long param : params) {
            if (null == param) {
                logger.error("入参存在null值");
                throw new CustomException(ErrorCodeEnum.Param_can_not_null);
            }
        }
    }
}
