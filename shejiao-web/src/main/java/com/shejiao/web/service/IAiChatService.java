package com.shejiao.web.service;

import ai.z.openapi.service.model.ChatMessage;
import com.shejiao.web.domain.entity.AiChatMessage;
import com.shejiao.web.domain.entity.AiChatSession;

import java.util.List;

/**
 * AI对话服务接口
 */
public interface IAiChatService {

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @param title  会话标题
     * @return 会话ID
     */
    String createSession(String userId, String title);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiChatSession> getUserSessions(String userId);

    /**
     * 获取所有会话列表（管理系统用）
     *
     * @param username 用户名（可选）
     * @param title    会话标题（可选）
     * @return 会话列表
     */
    List<AiChatSession> getAllSessions(String username, String title);

    /**
     * 获取所有消息列表（管理系统用）
     *
     * @param sessionId 会话ID（可选）
     * @param role      消息角色（可选）
     * @return 消息列表
     */
    List<AiChatMessage> getAllMessages(String sessionId, String role);

    /**
     * 批量删除会话（管理系统用）
     *
     * @param sessionIds 会话ID列表
     * @return 是否成功
     */
    boolean batchDeleteSessions(List<String> sessionIds);

    /**
     * 批量删除消息（管理系统用）
     *
     * @param messageIds 消息ID列表
     * @return 是否成功
     */
    boolean batchDeleteMessages(List<String> messageIds);

    /**
     * 获取会话的历史消息（转换为ChatMessage格式）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于权限校验）
     * @return 消息列表
     */
    List<ChatMessage> getSessionHistory(String sessionId, String userId);

    /**
     * 保存用户消息
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param content   消息内容
     * @param model     使用的模型版本
     * @return 保存的消息
     */
    AiChatMessage saveUserMessage(String sessionId, String userId, String content, String model);

    /**
     * 保存AI回复消息
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param content   消息内容
     * @param model     使用的模型版本
     * @return 保存的消息
     */
    AiChatMessage saveAssistantMessage(String sessionId, String userId, String content, String model);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于权限校验）
     * @return 是否成功
     */
    boolean deleteSession(String sessionId, String userId);

    /**
     * 删除会话（管理系统用，无权限校验）
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteSession(String sessionId);

    /**
     * 删除消息（管理系统用，无权限校验）
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean deleteMessage(String messageId);

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于权限校验）
     * @return 会话信息
     */
    AiChatSession getSession(String sessionId, String userId);

    /**
     * 获取会话详情（管理系统用，无权限校验）
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    AiChatSession getSession(String sessionId);
}
