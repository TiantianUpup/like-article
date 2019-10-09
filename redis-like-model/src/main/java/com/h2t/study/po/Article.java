package com.h2t.study.po;

import com.baomidou.mybatisplus.annotation.TableField;

import java.sql.Blob;

/**
 * 文章实体
 *
 * @author hetiantian
 * @since 2019-10-08
 */
public class Article extends BasePO {
    /**
     * 文章内容
     */
    @TableField(value = "content", exist = true)
    private Blob content;

    /**
     * 文章总点赞数
     */
    @TableField(value = "total_like_count", exist = true)
    private Long totalLikeCount;

    public Article() {
        super();
    }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    public Long getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(Long totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }
}
