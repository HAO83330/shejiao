package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author shejiao
 */
@Data
@TableName("web_mention")
public class WebMention {
    /**
     * 主键
     */
    private String id;

    /**
     * 笔记id
     */
    private String nid;

    /**
     * 发布笔记用户id
     */
    private String noteUid;

    /**
     * 被@的用户id
     */
    private String uid;

    /**
     * @人的用户id
     */
    private String mentionUid;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 创建用户
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改用户
     */
    private String updater;

    /**
     * 修改时间
     */
    private Date updateTime;
}
