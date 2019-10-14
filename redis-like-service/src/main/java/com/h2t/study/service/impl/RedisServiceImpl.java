package com.h2t.study.service.impl;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.service.RedisService;
import com.h2t.study.util.FastjsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final String TOTAL_LIKE_COUNT_KEY = "TOTAL:LIKE:COUNT";

    /**
     * 用户点赞文章key
     */
    private final String USER_LIKE_ARTICLE_KEY = "USER:LIKE:ARTICLE";

    /**
     * 文章被点赞的key
     */
    private final String ARTICLE_LIKED_USER_KEY = "ARTICLE:LIKED:USER";

    @Resource
    private RedisTemplate redisTemplate;

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
    public void likeArticle(Long articleId, Long likedUserId, Long likedPostId) {
        validateParam(articleId, likedUserId, likedPostId);  //参数验证
        //只有未点赞的用户才可以进行点赞
        likeArticleLogicValidate(articleId, likedUserId, likedPostId);

        logger.info("点赞数据存入redis开始，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        try {
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
            System.out.println("print...");
            System.out.println(likePostIdSet);
            likePostIdSet.add(likedPostId);
            System.out.println(likePostIdSet);
            redisTemplate.opsForHash().put(ARTICLE_LIKED_USER_KEY, String.valueOf(articleId), FastjsonUtil.serialize(likePostIdSet));

            logger.info("取消点赞数据存入redis结束，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
        } catch (Exception e) {
            logger.error("点赞执行过程中出错将进行回滚，articleId:{}，likedUserId:{}，likedPostId:{}，errorMsg:{}",
                    articleId, likedUserId, likedPostId, e.getMessage());
            throw e;
        }
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
        validateParam(articleId, likedUserId, likedPostId);  //参数校验
        List<Long> result;

        //只有点赞的用户才可以取消点赞
        if (!redisTemplate.opsForSet().isMember(String.format("user_%d", likedUserId), String.valueOf(articleId))) {
            logger.error("该文章未被当前用户点赞，不可以进行取消点赞操作，articleId:{}，likedUserId:{}，likedPostId:{}",
                    articleId, likedUserId, likedPostId);
            throw new CustomException(ErrorCodeEnum.Unlike_article_not_exist);
        }

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

    /**
     * 点赞文章逻辑校验
     *
     * @throws
     */
    private void likeArticleLogicValidate(Long articleId, Long likedUserId, Long likedPostId) {
        Set<Long> set = FastjsonUtil.deserializeToSet((String) redisTemplate.opsForHash().get(USER_LIKE_ARTICLE_KEY, String.valueOf(likedPostId)), Long.class);
        if (set == null) {
            return;
        } else {
            if (set.contains(articleId)) {
                logger.error("该文章已被当前用户点赞，重复点赞，articleId:{}，likedUserId:{}，likedPostId:{}", articleId, likedUserId, likedPostId);
                throw new CustomException(ErrorCodeEnum.Like_article_is_exist);
            }
        }
    }
}
