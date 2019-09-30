package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    @Resource
    private RedisTemplate redisTemplate;

    private ValueOperations<String, String> valueOperations;

    private SetOperations<String, String> setOperations;

    /**
     * 初始化valueOperations、setOperations
     */
    @PostConstruct
    public void init() {
        valueOperations = redisTemplate.opsForValue();
        setOperations = redisTemplate.opsForSet();
    }

    /**
     * 用户点赞某篇文章 TODO 事务需要考虑、分布式部署是不是需考虑分布式事务
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long likeArticle(Long articleId, Long userId) {
        if (null == userId || null == articleId) {
            throw new CustomException(ErrorCodeEnum.Param_can_not_null);
        }

        //1.用户总点赞数+1
        //TODO 并发需要考虑 ==null
        if (valueOperations.get(userId) == null) {
            valueOperations.set(String.valueOf(userId), "1");
        } else {
            valueOperations.increment(String.valueOf(userId), 1);

        }
        //2.用户喜欢的文章+1
        setOperations.add(String.format("%slike", userId), String.valueOf(articleId));
        //3.文章点赞数+1
        return setOperations.add(String.valueOf(articleId), String.valueOf(userId));
    }

    /**
     * 取消点赞 TODO 事务
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return
     */
    public Long unlikeArticle(Long articleId, Long userId) {
        //1.用户总点赞数-1
        valueOperations.decrement(String.valueOf(userId), 1);
        //2.用户喜欢的文章-1
        setOperations.remove(String.format("%slike", userId), String.valueOf(articleId));
        //3.取消用户某篇文章的点赞数
        return setOperations.remove(String.valueOf(articleId), String.valueOf(userId));
    }

    /**
     * 统计某篇文章总点赞数
     *
     * @param articleId
     * @return
     */
    public Long countArticleLike(Long articleId) {
        return setOperations.size(String.valueOf(articleId));
    }

    /**
     * 统计用户总的文章点赞数
     *
     * @param userId
     * @return
     */
    public Long countUserLike(Long userId) {
        return setOperations.size(String.valueOf(userId));
    }

    /**
     * 获取用户点赞的文章
     *
     * @param userId 用户ID
     * @return
     */
    public List<Long> getUserLikeArticleIds(Long userId) {
        String userKey = String.format("%slike", userId);
        Set<String> articleIdSet = setOperations.members(userKey);
        return articleIdSet.stream()
                .map(s -> Long.parseLong(s)).collect(Collectors.toList());
    }
}
