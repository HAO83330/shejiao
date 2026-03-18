package com.shejiao.web.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author shejiao
 */
@Data
public class MentionVO {
    /**
     * 主键
     */
    private String id;

    /**
     * 笔记id
     */
    private String nid;

    /**
     * 被@的用户id
     */
    private String uid;

    /**
     * @人的用户id
     */
    private String mentionUid;

    /**
     * @人的用户名
     */
    private String username;

    /**
     * @人的头像
     */
    private String avatar;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 笔记封面
     */
    private String noteCover;

    /**
     * 时间
     */
    private long time;
}
