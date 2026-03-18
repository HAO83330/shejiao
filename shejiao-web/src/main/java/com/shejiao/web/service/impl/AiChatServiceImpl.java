package com.shejiao.web.service.impl;

import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shejiao.common.utils.StringUtilss;
import com.shejiao.web.domain.entity.AiChatMessage;
import com.shejiao.web.domain.entity.AiChatSession;
import com.shejiao.web.mapper.AiChatMessageMapper;
import com.shejiao.web.mapper.AiChatSessionMapper;
import com.shejiao.web.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AI对话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSession(String userId, String title) {
        // 获取当前最大的会话ID
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AiChatSession::getId);
        wrapper.orderByDesc(AiChatSession::getId);
        wrapper.last("LIMIT 1");
        AiChatSession lastSession = sessionMapper.selectOne(wrapper);
        
        // 生成新的递增ID
        long newId = 1;
        if (lastSession != null) {
            try {
                newId = Long.parseLong(lastSession.getId()) + 1;
            } catch (NumberFormatException e) {
                // 如果ID不是数字格式，从1开始
                newId = 1;
            }
        }
        
        // 确保ID唯一，防止并发冲突
        while (true) {
            LambdaQueryWrapper<AiChatSession> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(AiChatSession::getId, String.valueOf(newId));
            if (sessionMapper.selectCount(checkWrapper) == 0) {
                break;
            }
            newId++;
        }
        
        AiChatSession session = new AiChatSession();
        session.setId(String.valueOf(newId));
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setStatus(0);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public List<AiChatSession> getUserSessions(String userId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId)
                .eq(AiChatSession::getStatus, 0)
                .orderByDesc(AiChatSession::getUpdateTime);
        return sessionMapper.selectList(wrapper);
    }

    @Override
    public List<AiChatSession> getAllSessions(String username, String title) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getStatus, 0);
        
        // 这里简化处理，实际项目中可能需要关联用户表查询用户名
        if (title != null && !title.isEmpty()) {
            wrapper.like(AiChatSession::getTitle, title);
        }
        
        wrapper.orderByDesc(AiChatSession::getUpdateTime);
        return sessionMapper.selectList(wrapper);
    }

    @Override
    public List<AiChatMessage> getAllMessages(String sessionId, String role) {
        // 先获取所有活跃状态的会话ID
        LambdaQueryWrapper<AiChatSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(AiChatSession::getStatus, 0);
        sessionWrapper.select(AiChatSession::getId);
        List<AiChatSession> activeSessions = sessionMapper.selectList(sessionWrapper);
        List<String> activeSessionIds = activeSessions.stream()
                .map(AiChatSession::getId)
                .collect(java.util.stream.Collectors.toList());
        
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        
        if (sessionId != null && !sessionId.isEmpty()) {
            // 如果指定了会话ID，先检查该会话是否活跃
            if (activeSessionIds.contains(sessionId)) {
                wrapper.eq(AiChatMessage::getSessionId, sessionId);
            } else {
                // 如果会话不活跃，返回空列表
                return new ArrayList<>();
            }
        } else {
            // 如果没有指定会话ID，只查询活跃会话的消息
            if (!activeSessionIds.isEmpty()) {
                wrapper.in(AiChatMessage::getSessionId, activeSessionIds);
            } else {
                // 如果没有活跃会话，返回空列表
                return new ArrayList<>();
            }
        }
        
        if (role != null && !role.isEmpty()) {
            wrapper.eq(AiChatMessage::getRole, role);
        }
        
        // 按照创建时间降序排序，最新消息在前面
        wrapper.orderByDesc(AiChatMessage::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSessions(List<String> sessionIds) {
        for (String sessionId : sessionIds) {
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session != null) {
                // 软删除会话
                session.setStatus(1);
                session.setUpdateTime(new Date());
                sessionMapper.updateById(session);
                
                // 删除该会话下的所有消息
                LambdaQueryWrapper<AiChatMessage> messageWrapper = new LambdaQueryWrapper<>();
                messageWrapper.eq(AiChatMessage::getSessionId, sessionId);
                messageMapper.delete(messageWrapper);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMessages(List<String> messageIds) {
        for (String messageId : messageIds) {
            messageMapper.deleteById(messageId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(String sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return false;
        }

        // 软删除会话
        session.setStatus(1);
        session.setUpdateTime(new Date());
        sessionMapper.updateById(session);
        
        // 删除该会话下的所有消息
        LambdaQueryWrapper<AiChatMessage> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(AiChatMessage::getSessionId, sessionId);
        messageMapper.delete(messageWrapper);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(String messageId) {
        return messageMapper.deleteById(messageId) > 0;
    }

    @Override
    public AiChatSession getSession(String sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public List<ChatMessage> getSessionHistory(String sessionId, String userId) {
        // 校验会话归属
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return new ArrayList<>();
        }

        List<AiChatMessage> messages = messageMapper.selectBySessionId(sessionId);
        List<ChatMessage> chatMessages = new ArrayList<>();

        for (AiChatMessage msg : messages) {
            chatMessages.add(ChatMessage.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build());
        }

        return chatMessages;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatMessage saveUserMessage(String sessionId, String userId, String content, String model) {
        AiChatMessage message = saveMessage(sessionId, userId, ChatMessageRole.USER.value(), content, model);
        
        // 如果是该会话的第一条消息，自动更新会话标题
        updateSessionTitleIfFirstMessage(sessionId, content);
        
        return message;
    }
    
    /**
     * 如果是会话的第一条消息，自动更新会话标题
     * 标题取消息内容的前20个字符
     */
    private void updateSessionTitleIfFirstMessage(String sessionId, String content) {
        // 查询该会话的消息数量
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId);
        long messageCount = messageMapper.selectCount(wrapper);
        
        // 如果只有一条消息（刚保存的这条），则更新标题
        if (messageCount == 1) {
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session != null && ("新对话".equals(session.getTitle()) || session.getTitle() == null)) {
                // 取内容前20个字符作为标题
                String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
                session.setTitle(title);
                session.setUpdateTime(new Date());
                sessionMapper.updateById(session);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatMessage saveAssistantMessage(String sessionId, String userId, String content, String model) {
        AiChatMessage message = saveMessage(sessionId, userId, ChatMessageRole.ASSISTANT.value(), content, model);
        
        // 更新会话时间
        AiChatSession session = new AiChatSession();
        session.setId(sessionId);
        session.setUpdateTime(new Date());
        sessionMapper.updateById(session);
        
        return message;
    }

    private AiChatMessage saveMessage(String sessionId, String userId, String role, String content, String model) {
        Integer maxOrder = messageMapper.selectMaxOrderBySessionId(sessionId);
        
        // 获取当前最大的消息ID
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AiChatMessage::getId);
        wrapper.orderByDesc(AiChatMessage::getId);
        wrapper.last("LIMIT 1");
        AiChatMessage lastMessage = messageMapper.selectOne(wrapper);
        
        // 生成新的递增ID
        long newId = 1;
        if (lastMessage != null) {
            try {
                newId = Long.parseLong(lastMessage.getId()) + 1;
            } catch (NumberFormatException e) {
                // 如果ID不是数字格式，从1开始
                newId = 1;
            }
        }
        
        // 确保ID唯一，防止并发冲突
        while (true) {
            LambdaQueryWrapper<AiChatMessage> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(AiChatMessage::getId, String.valueOf(newId));
            if (messageMapper.selectCount(checkWrapper) == 0) {
                break;
            }
            newId++;
        }
        
        AiChatMessage message = new AiChatMessage();
        message.setId(String.valueOf(newId));
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setStatus("success"); // 默认设置为成功状态
        message.setModel(model); // 设置使用的模型版本
        message.setMessageOrder(maxOrder + 1);
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        
        messageMapper.insert(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(String sessionId, String userId) {
        try {
            // 检查参数
            if (sessionId == null || sessionId.isEmpty() || userId == null || userId.isEmpty()) {
                return false;
            }
            
            // 查询会话
            AiChatSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                return false;
            }
            
            // 检查会话归属
            if (!session.getUserId().equals(userId)) {
                return false;
            }

            // 软删除会话
            session.setStatus(1);
            session.setUpdateTime(new Date());
            sessionMapper.updateById(session);
            
            // 删除该会话下的所有消息
            LambdaQueryWrapper<AiChatMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(AiChatMessage::getSessionId, sessionId);
            messageMapper.delete(messageWrapper);
            
            return true;
        } catch (Exception e) {
            // 捕获所有异常，返回false
            return false;
        }
    }

    @Override
    public AiChatSession getSession(String sessionId, String userId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return null;
        }
        return session;
    }
}
