package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.common.annotation.Excel;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * AI聊天会话
 *
 * @Author shejiao
 */
@Data
@TableName("ai_chat_session")
public class AiChatSession extends shejiaoBaseEntity {

    /**
     * 用户ID
     */
    @Excel(name = "用户ID", sort = 1)
    private String userId;

    /**
     * 会话标题
     */
    @Excel(name = "会话标题", sort = 2)
    private String title;

    /**
     * 会话状态（0：正常 1：禁用）
     */
    @Excel(name = "会话状态", sort = 3)
    private Integer status;
}
