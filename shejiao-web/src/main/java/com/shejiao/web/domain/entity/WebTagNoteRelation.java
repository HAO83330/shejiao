package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 标签-笔记
 *
 * @Author shejiao
 */
@Data
@TableName("web_tag_note_relation")
public class WebTagNoteRelation extends shejiaoBaseEntity {

    /**
     * 笔记ID
     */
    private String nid;

    /**
     * 标签ID
     */
    private String tid;
}
