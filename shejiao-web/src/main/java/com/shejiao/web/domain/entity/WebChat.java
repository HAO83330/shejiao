package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.common.annotation.Excel;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

/**
 * 聊天
 *
 * @Author shejiao
 */
@Data
@TableName("web_chat")
public class WebChat extends shejiaoBaseEntity {

    /**
     * 发送方用户ID
     */
    @Excel(name = "发送方ID")
    private String sendUid;

    /**
     * 接收方用户ID
     */
    @Excel(name = "接收方ID")
    private String acceptUid;

    /**
     * 聊天内容
     */
    @Excel(name = "聊天内容")
    private String content;

    /**
     * 聊天类型（0：私聊 1：群聊）
     */
    @Excel(name = "聊天类型", readConverterExp = "0=私聊,1=群聊")
    private Integer chatType;

    /**
     * 信息类型（0：通知 1：文本 2：图片  3：自定义消息）
     */
    @Excel(name = "消息类型", readConverterExp = "0=通知,1=文本,2=图片,3=自定义消息")
    private Integer msgType;

    /**
     * 时间戳
     */
    @Excel(name = "时间戳")
    private long timestamp;
}
