package com.shejiao.web.domain.vo;

import com.shejiao.common.annotation.Excel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论
 *
 * @Author shejiao
 */
@Data
@Accessors(chain = true)
public class CommentVO implements Serializable {

    @Excel(name = "评论ID", sort = 1)
    private String id;

    private String pid;

    @Excel(name = "笔记ID", sort = 2)
    private String nid;

    @Excel(name = "笔记标题", sort = 3)
    private String title;

    @Excel(name = "笔记封面", sort = 4)
    private String noteCover;

    @Excel(name = "评论人ID", sort = 5)
    private String uid;

    @Excel(name = "评论人", sort = 6)
    private String username;

    @Excel(name = "评论人头像", sort = 7)
    private String avatar;

    private String noteUid;

    @Excel(name = "笔记作者", sort = 8)
    private String pushUsername;

    private String replyId;

    private String replyUid;

    @Excel(name = "被评论人", sort = 9)
    private String replyUsername;

    @Excel(name = "被评论人头像", sort = 10)
    private String replyAvatar;

    @Excel(name = "评论内容", sort = 11)
    private String content;

    @Excel(name = "回复内容", sort = 12)
    private String replyContent;

    @Excel(name = "评论等级", sort = 13)
    private Integer level;

    private Long time;

    @Excel(name = "评论时间", sort = 14, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Excel(name = "点赞数", sort = 15)
    private Long likeCount;

    private Boolean isLike;

    @Excel(name = "二级评论数", sort = 16)
    private Long twoCommentCount;

    private List<CommentVO> children;

}
