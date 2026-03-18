package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.common.annotation.Excel;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 笔记
 *
 * @Author shejiao
 */
@Data
@TableName("web_note")
public class WebNote extends shejiaoBaseEntity {

    /**
     * 笔记标题
     */
    @Excel(name = "笔记标题")
    private String title;

    /**
     * 笔记内容
     */
    @Excel(name = "笔记内容")
    private String content;

    /**
     * 笔记封面
     */
    @Excel(name = "笔记封面", cellType = Excel.ColumnType.IMAGE)
    private String noteCover;

    /**
     * 作者
     */
    @Excel(name = "作者")
    private String author;

    /**
     * 笔记一级分类ID
     */
    @Excel(name = "分类ID")
    private String cpid;

    /**
     * 图片数量
     */
    @Excel(name = "图片数量")
    private Integer count;

    /**
     * 是否置顶
     */
    @Excel(name = "是否置顶")
    private String pinned;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private String auditStatus;

    /**
     * 驳回理由
     */
    @Excel(name = "驳回理由")
    private String rejectReason;

    /**
     * 笔记类型
     */
    @Excel(name = "笔记类型")
    private String noteType;

    /**
     * 点赞次数
     */
    @Excel(name = "点赞次数")
    private Long likeCount;

    /**
     * 收藏次数
     */
    @Excel(name = "收藏次数")
    private Long collectionCount;

    /**
     * 评论次数
     */
    @Excel(name = "评论次数")
    private Long commentCount;

    /**
     * 浏览次数
     */
    @Excel(name = "浏览次数")
    private Long viewCount;

    /**
     * 笔记高度
     */
    private Integer noteCoverHeight;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 笔记二级分类ID
     */
    private String cid;

    /**
     * 笔记urls
     */
    private String urls;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 笔记状态
     */
    private String status;

    /**
     * 时间戳
     */
    private Long time;
}
