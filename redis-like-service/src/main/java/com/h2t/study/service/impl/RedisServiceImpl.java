package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import com.h2t.study.task.AsynchronousTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
    Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private AsynchronousTask asynchronousTask;

    /**
     * 指定序列化方式、开启事务
     */
    @PostConstruct
    public void init() {
        redisTemplate.setEnableTransactionSupport(true);
        RedisSerializer redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);
    }

    /**
     * 用户点赞某篇文章
     *
     * @param likedUserId 被点赞用户ID
     * @param likedPostId 点赞用户
     * @param articleId 文章ID
     * @return
     */
    public List<Long> likeArticle(Long articleId, Long likedUserId, Long likedPostId) {
        validateParam(articleId, likedUserId, likedPostId);  //参数验证
        logger.info("点赞数据异步入库任务开始，articleId:{}，likedPostId:{}", articleId, likedPostId);
        asynchronousTask.likeArticleToDB(articleId, likedPostId);  //异步入库

        List<Long> result;
        logger.info("点赞数据存入redis开始，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        redisTemplate.multi();  //开启事务
        try {
            //1.用户总点赞数+1
            redisTemplate.opsForValue().increment(String.valueOf(likedUserId), 1);

            //2.用户喜欢的文章+1
            redisTemplate.opsForSet().add(String.format("user_%d", likedPostId), String.valueOf(articleId));
            //3.文章点赞数+1
            redisTemplate.opsForSet().add(String.format("article_%d", articleId), String.valueOf(likedPostId));
            result = redisTemplate.exec();  //执行事务
            logger.info("取消点赞数据存入redis结束，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        } catch (Exception e) {
            logger.error("点赞执行过程中出错将进行回滚，articleId:{}，likedUserId:{}，likedPostId:{}，errorMsg:{}",
                    articleId, likedUserId, likedPostId, e.getMessage());
            redisTemplate.discard();  //回滚
            throw e;
        }

        return result;
    }

    /**
     * 取消点赞
     *
     * @param likedUserId 被点赞用户ID
     * @param likedPostId 点赞用户
     * @param articleId 文章ID
     * @return
     */
    public List<Long> unlikeArticle(Long articleId, Long likedUserId, Long likedPostId) {
        validateParam(articleId, likedUserId, likedPostId);
        List<Long> result;

        logger.info("取消点赞数据异步入库任务开始，articleId:{}，likedPostId:{}", articleId, likedPostId);
        asynchronousTask.unlikeArticleToDB(articleId, likedPostId);  //异步入库任务

        logger.info("取消点赞数据存入redis开始，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        redisTemplate.multi();  //开启事务
        try {
            //1.用户总点赞数-1
            redisTemplate.opsForValue().decrement(String.valueOf(likedUserId), 1);
            //2.用户喜欢的文章-1
            redisTemplate.opsForSet().remove(String.format("user_%d", likedPostId), String.valueOf(articleId));
            //3.取消用户某篇文章的点赞数
            redisTemplate.opsForSet().remove(String.format("article_%d", articleId), String.valueOf(likedPostId));
            result = redisTemplate.exec(); //执行命令
            logger.info("取消点赞数据存入redis结束，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        } catch (Exception e) {
            logger.error("取消点赞执行过程中出错将进行回滚，articleId:{}，likedUserId:{}，likedPostId:{}，errorMsg:{}",
                    articleId, likedUserId, likedPostId, e.getMessage());
            redisTemplate.discard();
            throw e;
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
        return redisTemplate.opsForSet().size(String.format("article_%d", articleId));
    }

    /**
     * 统计用户总的文章点赞数
     *
     * @param likedUserId
     * @return
     */
    public Long countUserLike(Long likedUserId) {
        validateParam(likedUserId);
        String result = (String) redisTemplate.opsForValue().get(String.valueOf(likedUserId));

        return result == null ? 0 : Long.parseLong(result);
    }

    /**
     * 获取用户点赞的文章
     *
     * @param likedPostId
     * @return
     */
    public List<Long> getUserLikeArticleIds(Long likedPostId) {
        validateParam(likedPostId);
        String userKey = String.format("user_%d", likedPostId);
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
