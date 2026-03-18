package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.common.annotation.Excel;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * AI对话消息
 *
 * @Author shejiao
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage extends shejiaoBaseEntity {

    /**
     * 会话ID
     */
    @Excel(name = "会话ID", sort = 1)
    private String sessionId;

    /**
     * 用户ID
     */
    @Excel(name = "用户ID", sort = 2)
    private String userId;

    /**
     * 消息角色（user/assistant/system）
     */
    @Excel(name = "消息角色", sort = 3)
    private String role;

    /**
     * 消息内容
     */
    @Excel(name = "消息内容", sort = 4)
    private String content;

    /**
     * 消息顺序
     */
    @Excel(name = "消息顺序", sort = 5)
    private Integer messageOrder;

    /**
     * 消息状态（success/failed）
     */
    @Excel(name = "消息状态", sort = 6)
    private String status;

    /**
     * 使用的模型版本
     */
    @Excel(name = "模型版本", sort = 7)
    private String model;
}
