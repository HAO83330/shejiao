package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 标签
 *
 * @Author shejiao
 */
@Data
@TableName("web_tag")
public class WebTag extends shejiaoBaseEntity {

    /**
     * 使用次数
     */
    private Long likeCount;

    /**
     * 标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer sort;
}
