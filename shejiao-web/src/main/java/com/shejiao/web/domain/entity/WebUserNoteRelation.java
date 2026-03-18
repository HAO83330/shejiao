package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 用户-笔记
 *
 * @Author shejiao
 */
@Data
@TableName("web_user_note_relation")
public class WebUserNoteRelation extends shejiaoBaseEntity {

    /**
     * 笔记ID
     */
    private String nid;

    /**
     * 用户ID
     */
    private String uid;
}
