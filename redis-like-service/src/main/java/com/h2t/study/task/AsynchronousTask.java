package com.h2t.study.task;

import com.h2t.study.enums.ErrorCodeEnum;
import com.h2t.study.exception.CustomException;
import com.h2t.study.po.Article;
import com.h2t.study.po.UserLikeArticle;
import com.h2t.study.service.ArticleService;
import com.h2t.study.service.UserLikeArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 异步落库任务
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/10/08 16:17
 */
@Service
public class AsynchronousTask {
    Logger logger = LoggerFactory.getLogger(AsynchronousTask.class);

    @Resource
    private ArticleService articleService;

    @Resource
    private UserLikeArticleService userLikeArticleService;

    /**
     * 用户点赞某篇文章数据落库
     *
     * @param likedPostId 点赞用户
     * @param articleId   文章ID
     */
    public synchronized void likeArticleToDB(Long articleId, Long likedPostId) {
        new Thread(() -> {
            try {
                //1.文章总点赞数落库+1
                Article article = articleService.getById(articleId);
                if (article == null) {
                    logger.error("点赞文章不存在，articleId{}", articleId);
                    throw new CustomException(ErrorCodeEnum.Object_can_not_found);
                }

                Long totalLikeCount = article.getTotalLikeCount();
                article.setTotalLikeCount(++totalLikeCount);
                articleService.modifyById(article);
                //2.用户点赞文章关联
                UserLikeArticle userLikeArticle = new UserLikeArticle();
                userLikeArticle.setUserId(likedPostId);
                userLikeArticle.setArticleId(articleId);
                userLikeArticleService.insert(userLikeArticle);

                throw new CustomException(ErrorCodeEnum.Unlike_article_not_exist);
            } catch (Exception e) {
                logger.error("点赞入库出错，exception:{}", e);
                throw e;
            }

        }).start();

    }

    /**
     * 用户取消点赞某篇文章数据落库
     *
     * @param likedPostId 点赞用户
     * @param articleId   文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void unlikeArticleToDB(Long articleId, Long likedPostId) {
        new Thread(() -> {
            try {
                //1.文章总点赞数落库-1
                Article article = articleService.getById(articleId);
                if (article == null) {
                    logger.error("取消点赞文章不存在，articleId{}", articleId);
                    throw new CustomException(ErrorCodeEnum.Object_can_not_found);
                }

                Long totalLikeCount = article.getTotalLikeCount();
                article.setTotalLikeCount(--totalLikeCount);
                articleService.modifyById(article);
                //2.取消用户点赞文章关联
                UserLikeArticle userLikeArticle = new UserLikeArticle();
                userLikeArticle.setUserId(likedPostId);
                userLikeArticle.setArticleId(articleId);
                userLikeArticleService.delete(userLikeArticle);
            } catch (Exception e) {
                logger.error("取消点赞入库出错，exception:{}", e);
                throw e;
            }

        }).start();
    }
}
