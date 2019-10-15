package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import com.h2t.study.util.FastjsonUtil;
import org.apache.ibatis.annotations.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
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

    /**
     * 文章点赞总数key
     * redis key命名规范推荐使用大写，单词与单词之间使用:
     */
    @Value("total.like.count.key")
    private String TOTAL_LIKE_COUNT_KEY;

    /**
     * 用户点赞文章key
     */
    @Value("user.like.article.key")
    private String USER_LIKE_ARTICLE_KEY;

    /**
     * 文章被点赞的key
     */
    @Value("article.liked.user.key")
    private String ARTICLE_LIKED_USER_KEY;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 指定序列化方式
     */
    @PostConstruct
    public void init() {
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
     */
    public void likeArticle(Long articleId, Long likedUserId, Long likedPostId) {
        validateParam(articleId, likedUserId, likedPostId);  //参数验证
        //只有未点赞的用户才可以进行点赞
        likeArticleLogicValidate(articleId, likedUserId, likedPostId);

        logger.info("点赞数据存入redis开始，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        //1.用户总点赞数+1
        redisTemplate.opsForHash().increment(TOTAL_LIKE_COUNT_KEY, String.valueOf(likedUserId), 1);

        //2.用户喜欢的文章+1
        String userLikeResult = (String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId));
        Set<Long> articleIdSet = userLikeResult == null ? new HashSet<>() : FastjsonUtil.deserializeToSet(userLikeResult, Long.class);
        articleIdSet.add(articleId);
        redisTemplate.opsForHash().put(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId), FastjsonUtil.serialize(articleIdSet));

        //3.文章点赞数+1
        String articleLikedResult = (String) redisTemplate.opsForHash().get(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId));
        Set<Long> likePostIdSet = articleLikedResult == null ? new HashSet<>() : FastjsonUtil.deserializeToSet(articleLikedResult, Long.class);
        likePostIdSet.add(likedPostId);
        redisTemplate.opsForHash().put(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId), FastjsonUtil.serialize(likePostIdSet));
        logger.info("取消点赞数据存入redis结束，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
    }

    /**
     * 取消点赞
     *
     * @param likedUserId 被点赞用户ID
     * @param likedPostId 点赞用户
     * @param articleId 文章ID
     */
    public void unlikeArticle(Long articleId, Long likedUserId, Long likedPostId) {
        validateParam(articleId, likedUserId, likedPostId);  //参数校验

        logger.info("取消点赞数据存入redis开始，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        //1.用户总点赞数-1
        synchronized (this) {
            //只有点赞的用户才可以取消点赞
            unlikeArticleLogicValidate(articleId, likedUserId, likedPostId);
            Long totalLikeCount = (Long) redisTemplate.opsForHash().get(TOTAL_LIKE_COUNT_KEY, String.valueOf(likedUserId));
            redisTemplate.opsForValue().set(TOTAL_LIKE_COUNT_KEY, String.valueOf(likedUserId), --totalLikeCount);

            //2.用户喜欢的文章-1
            String userLikeResult = (String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId));
            Set<Long> articleIdSet = FastjsonUtil.deserializeToSet(userLikeResult, Long.class);
            articleIdSet.remove(articleId);
            redisTemplate.opsForHash().put(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId), FastjsonUtil.serialize(articleIdSet));

            //3.取消用户某篇文章的点赞数
            String articleLikedResult = (String) redisTemplate.opsForHash().get(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId));
            Set<Long> likePostIdSet = FastjsonUtil.deserializeToSet(articleLikedResult, Long.class);
            likePostIdSet.remove(likedPostId);
            redisTemplate.opsForHash().put(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId), FastjsonUtil.serialize(likePostIdSet));
        }

        logger.info("取消点赞数据存入redis结束，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
    }

    /**
     * 统计某篇文章总点赞数
     *
     * @param articleId 文章ID
     * @return
     */
    public synchronized Long countArticleLike(Long articleId) {
        validateParam(articleId);
        String articleLikedResult = (String) redisTemplate.opsForHash().get(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId));
        Set<Long> likePostIdSet = FastjsonUtil.deserializeToSet(articleLikedResult, Long.class);
        return new Long(likePostIdSet.size());
    }

    /**
     * 统计用户总的文章点赞数
     *
     * @param likedUserId
     * @return
     */
    public synchronized Long countUserLike(Long likedUserId) {
        validateParam(likedUserId);
        return Long.parseLong((String) redisTemplate.opsForHash().get(TOTAL_LIKE_COUNT_KEY, String.valueOf(likedUserId)));
    }

    /**
     * 获取用户点赞的文章
     *
     * @param likedPostId
     * @return
     */
    public List<Long> getUserLikeArticleIds(Long likedPostId) {
        validateParam(likedPostId);
        String userLikeResult = (String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId));
        Set<Long> articleIdSet = FastjsonUtil.deserializeToSet(userLikeResult, Long.class);

        return articleIdSet.stream().collect(Collectors.toList());
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

    /**
     * 点赞文章逻辑校验
     *
     * @throws
     */
    private void likeArticleLogicValidate(Long articleId, Long likedUserId, Long likedPostId) {
        Set<Long> articleIdSet = FastjsonUtil.deserializeToSet((String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId)), Long.class);
        Set<Long> likePostIdSet = FastjsonUtil.deserializeToSet((String) redisTemplate.opsForHash().get(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId)), Long.class);
        if (articleIdSet == null) {
            return;
        }
        if (likePostIdSet == null) {
            return;
        } else {
            if (articleIdSet.contains(articleId) || likePostIdSet.contains(likedPostId)) {
                logger.error("该文章已被当前用户点赞，重复点赞，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
                throw new CustomException(ErrorCodeEnum.Like_article_is_exist);
            }
        }
    }

    /**
     * 取消点赞逻辑校验
     */
    private void unlikeArticleLogicValidate(Long articleId, Long likedUserId, Long likedPostId) {
        Set<Long> articleIdSet = FastjsonUtil.deserializeToSet((String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId)), Long.class);
        Set<Long> likePostIdSet = FastjsonUtil.deserializeToSet((String) redisTemplate.opsForHash().get(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId)), Long.class);
        if (articleIdSet == null || !articleIdSet.contains(articleId)
                || likePostIdSet == null || !likePostIdSet.contains(likedPostId)) {
            logger.error("该文章未被当前用户点赞，不可以进行取消点赞操作，articleId:{}，likedUserId:{}，likedPostId:{}",
                    articleId, likedUserId, likedPostId);
            throw new CustomException(ErrorCodeEnum.Unlike_article_not_exist);
        }
    }
}
