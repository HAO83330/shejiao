package com.shejiao.web.domain.vo;

import com.shejiao.common.annotation.Excel;

/**
 * 聊天导出VO
 *
 * @Author shejiao
 */
public class WebChatExportVO {

    /**
     * 发送方用户ID
     */
    @Excel(name = "发送方ID")
    private String sendUid;

    /**
     * 发送方用户名
     */
    @Excel(name = "发送方名称")
    private String senderName;

    /**
     * 接收方用户ID
     */
    @Excel(name = "接收方ID")
    private String acceptUid;

    /**
     * 接收方用户名
     */
    @Excel(name = "接收方名称")
    private String receiverName;

    /**
     * 聊天内容
     */
    @Excel(name = "聊天内容")
    private String content;

    /**
     * 图片内容
     */
    @Excel(name = "图片", cellType = Excel.ColumnType.IMAGE)
    private String imageContent;

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

    /**
     * 创建时间
     */
    @Excel(name = "创建时间")
    private String createTime;

    // getters and setters
    public String getSendUid() {
        return sendUid;
    }

    public void setSendUid(String sendUid) {
        this.sendUid = sendUid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAcceptUid() {
        return acceptUid;
    }

    public void setAcceptUid(String acceptUid) {
        this.acceptUid = acceptUid;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
    }

    public Integer getChatType() {
        return chatType;
    }

    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}