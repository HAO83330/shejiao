package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 关注
 *
 * @Author shejiao
 */
@Data
@TableName("web_follow")
public class WebFollow extends shejiaoBaseEntity {

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 关注用户ID
     */
    private String fid;

}
