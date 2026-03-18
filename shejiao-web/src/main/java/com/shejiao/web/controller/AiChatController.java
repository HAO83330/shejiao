package com.shejiao.web.controller;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ModelData;
import ai.z.openapi.service.model.Choice;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.common.exception.ServiceException;
import com.shejiao.web.auth.AuthContextHolder;
import com.shejiao.web.domain.entity.AiChatMessage;
import com.shejiao.web.domain.entity.AiChatSession;
import com.shejiao.web.mapper.AiChatMessageMapper;
import com.shejiao.web.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class AiChatController {

    private final ZhipuAiClient zhipuAiClient;
    private final IAiChatService aiChatService;
    private final AiChatMessageMapper messageMapper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 获取当前登录用户ID（从AuthContextHolder中获取）
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // 从AuthContextHolder中获取用户ID（由UserLoginInterceptor设置）
        String userId = AuthContextHolder.getUserId();
        if (StringUtils.isEmpty(userId)) {
            // 如果没有登录，抛出ServiceException，返回401未授权错误
            throw new ServiceException("用户未登录，请先登录", ResultCodeEnum.TOKEN_FAIL.getCode());
        }
        return userId;
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session/create")
    public AjaxResult createSession(@RequestBody CreateSessionRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        String sessionId = aiChatService.createSession(userId, request.getTitle());
        return AjaxResult.success(sessionId);
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/session/list")
    public AjaxResult getUserSessions(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<AiChatSession> sessions = aiChatService.getUserSessions(userId);
        return AjaxResult.success(sessions);
    }

    /**
     * 获取会话的历史消息
     */
    @GetMapping("/session/history/{sessionId}")
    public AjaxResult getSessionHistory(@PathVariable String sessionId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<ChatMessage> messages = aiChatService.getSessionHistory(sessionId, userId);
        return AjaxResult.success(messages);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public AjaxResult deleteSession(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId(request);
            boolean success = aiChatService.deleteSession(sessionId, userId);
            return success ? AjaxResult.success() : AjaxResult.error("删除失败");
        } catch (ServiceException e) {
            // 处理服务异常
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            // 处理其他异常
            log.error("删除会话失败: {}", e.getMessage(), e);
            return AjaxResult.error("删除失败，请稍后重试");
        }
    }

    /**
     * 流式AI对话
     */
    @GetMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChat(@RequestParam String message, 
                                  @RequestParam(required = false) String sessionId,
                                  HttpServletRequest httpRequest) {
        // 检查用户登录状态
        String userId = AuthContextHolder.getUserId();
        log.info("[AI流式接口] 当前用户ID: {}, sessionId: {}", userId, sessionId);
        if (StringUtils.isEmpty(userId)) {
            log.warn("[AI流式接口] 用户未登录，拒绝访问");
            SseEmitter errorEmitter = new SseEmitter();
            try {
                errorEmitter.send(SseEmitter.event().name("error").data("用户未登录，请先登录"));
                errorEmitter.complete();
            } catch (IOException e) {
                errorEmitter.completeWithError(e);
            }
            return errorEmitter;
        }
        
        // 如果没有会话ID，创建新会话
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = aiChatService.createSession(userId, message.substring(0, Math.min(message.length(), 20)));
        }
        
        // 校验会话归属
        AiChatSession session = aiChatService.getSession(sessionId, userId);
        if (session == null) {
            SseEmitter errorEmitter = new SseEmitter();
            try {
                errorEmitter.send(SseEmitter.event().name("error").data("会话不存在或无权限"));
                errorEmitter.complete();
            } catch (IOException e) {
                errorEmitter.completeWithError(e);
            }
            return errorEmitter;
        }

        final String finalSessionId = sessionId;
        SseEmitter emitter = new SseEmitter(0L);
        
        executorService.execute(() -> {
            try {
                // 保存用户消息
                aiChatService.saveUserMessage(finalSessionId, userId, message, "glm-4-flash");
                
                // 构建消息列表（包含历史记录）
                List<ChatMessage> messages = buildMessagesWithHistory(finalSessionId, userId, message);
                
                ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                        .model("glm-4-flash")
                        .messages(messages)
                        .stream(true)
                        .maxTokens(2048)
                        .temperature(0.7f)
                        .build();

                ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

                if (response.isSuccess()) {
                    StringBuilder fullResponse = new StringBuilder();
                    
                    response.getFlowable().subscribe(
                        data -> {
                            try {
                                String content = extractContentFromStreamData(data);
                                if (content != null && !content.isEmpty()) {
                                    fullResponse.append(content);
                                    emitter.send(SseEmitter.event().data(content));
                                }
                            } catch (IOException e) {
                                log.error("发送SSE消息失败", e);
                            }
                        },
                        error -> {
                            log.error("AI流式响应错误", error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                // 保存AI回复
                                aiChatService.saveAssistantMessage(finalSessionId, userId, fullResponse.toString(), "glm-4-flash");
                                
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data("[DONE]"));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("发送完成事件失败", e);
                                emitter.complete();
                            }
                        }
                    );
                } else {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(response.getMsg()));
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("AI对话异常", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("服务异常，请稍后重试"));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onCompletion(() -> log.debug("AI对话SSE连接完成"));
        emitter.onTimeout(() -> log.warn("AI对话SSE连接超时"));
        emitter.onError((e) -> log.error("AI对话SSE连接错误", e));

        return emitter;
    }

    /**
     * 非流式AI对话
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId(httpRequest);
        log.info("[AI非流式接口] 当前用户ID: {}, sessionId: {}", userId, request.getSessionId());
        String sessionId = request.getSessionId();
        
        // 如果没有会话ID，创建新会话
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = aiChatService.createSession(userId, request.getMessage().substring(0, Math.min(request.getMessage().length(), 20)));
        }
        
        // 校验会话归属
        AiChatSession session = aiChatService.getSession(sessionId, userId);
        if (session == null) {
            return AjaxResult.error("会话不存在或无权限");
        }

        try {
            log.info("收到AI对话请求, 用户: {}, 会话: {}", userId, sessionId);
            
            // 保存用户消息
            aiChatService.saveUserMessage(sessionId, userId, request.getMessage(), "glm-4-flash");
            
            // 构建消息列表（包含历史记录）
            List<ChatMessage> messages = buildMessagesWithHistory(sessionId, userId, request.getMessage());
            
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model("glm-4-flash")
                    .messages(messages)
                    .stream(false)
                    .maxTokens(2048)
                    .temperature(0.7f)
                    .build();

            ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(params);

            log.info("AI响应成功: {}", response.isSuccess());
            
            if (response.isSuccess()) {
                String content = extractContentFromResponse(response);
                log.info("提取的内容: '{}', 长度: {}", content, content != null ? content.length() : 0);
                
                // 如果内容为空，返回错误
                if (content == null || content.trim().isEmpty()) {
                    log.error("AI返回内容为空");
                    return AjaxResult.error("AI返回内容为空");
                }
                
                // 保存AI回复
                aiChatService.saveAssistantMessage(sessionId, userId, content, "glm-4-flash");
                
                // 手动构建返回结果，确保data字段正确设置
                AjaxResult result = new AjaxResult();
                result.put("code", 200);
                result.put("msg", "操作成功");
                result.put("data", content);
                log.info("返回的AjaxResult: code={}, msg={}, data='{}'", result.get("code"), result.get("msg"), result.get("data"));
                return result;
            } else {
                log.error("AI服务返回错误: {}", response.getMsg());
                return AjaxResult.error("AI服务异常：" + response.getMsg());
            }
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return AjaxResult.error("服务异常，请稍后重试: " + e.getMessage());
        }
    }

    /**
     * 构建包含历史记录的消息列表
     */
    private List<ChatMessage> buildMessagesWithHistory(String sessionId, String userId, String currentMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统提示词
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM.value())
                .content("你是一个友好的AI助手，可以帮助用户解答问题、提供建议。请用简洁、专业的方式回答用户的问题。")
                .build());
        
        // 添加历史消息（最多保留最近20条）
        List<ChatMessage> history = aiChatService.getSessionHistory(sessionId, userId);
        int startIndex = Math.max(0, history.size() - 20);
        for (int i = startIndex; i < history.size(); i++) {
            messages.add(history.get(i));
        }
        
        // 当前用户消息
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER.value())
                .content(currentMessage)
                .build());
        
        return messages;
    }

    /**
     * 从流式数据中提取内容
     */
    private String extractContentFromStreamData(Object data) {
        try {
            if (data == null) return null;
            
            java.lang.reflect.Method getChoicesMethod = data.getClass().getMethod("getChoices");
            Object choices = getChoicesMethod.invoke(data);
            
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object choice = ((List<?>) choices).get(0);
                
                java.lang.reflect.Method getDeltaMethod = choice.getClass().getMethod("getDelta");
                Object delta = getDeltaMethod.invoke(choice);
                
                if (delta != null) {
                    java.lang.reflect.Method getContentMethod = delta.getClass().getMethod("getContent");
                    Object content = getContentMethod.invoke(delta);
                    return content != null ? content.toString() : null;
                }
            }
        } catch (Exception e) {
            log.debug("提取内容失败，尝试直接toString: {}", e.getMessage());
            return data.toString();
        }
        return null;
    }

    /**
     * 从响应中提取内容
     */
    private String extractContentFromResponse(ChatCompletionResponse response) {
        try {
            ModelData data = response.getData();
            if (data == null) {
                log.error("ModelData is null");
                return "";
            }
            
            List<Choice> choices = data.getChoices();
            if (choices == null || choices.isEmpty()) {
                log.error("Choices is null or empty");
                return "";
            }
            
            Choice choice = choices.get(0);
            ChatMessage message = choice.getMessage();
            if (message == null) {
                log.error("Message is null");
                return "";
            }
            
            Object content = message.getContent();
            return content != null ? content.toString() : "";
        } catch (Exception e) {
            log.error("提取响应内容失败", e);
            return "";
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/message/{messageId}")
    public AjaxResult deleteMessage(@PathVariable String messageId, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId(request);
            boolean success = aiChatService.deleteMessage(messageId);
            return success ? AjaxResult.success() : AjaxResult.error("删除失败");
        } catch (ServiceException e) {
            // 处理服务异常
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            // 处理其他异常
            log.error("删除消息失败: {}", e.getMessage(), e);
            return AjaxResult.error("删除失败，请稍后重试");
        }
    }

    /**
     * 根据会话ID和消息索引删除消息
     */
    @DeleteMapping("/message/{sessionId}/{index}")
    public AjaxResult deleteMessageByIndex(@PathVariable String sessionId, @PathVariable int index, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId(request);
            // 校验会话归属
            AiChatSession session = aiChatService.getSession(sessionId, userId);
            if (session == null) {
                return AjaxResult.error("会话不存在或无权限");
            }
            
            // 获取会话的所有消息（包含消息ID）
            List<AiChatMessage> messages = messageMapper.selectBySessionId(sessionId);
            if (index < 0 || index >= messages.size()) {
                return AjaxResult.error("消息索引无效");
            }
            
            // 获取要删除的消息
            AiChatMessage message = messages.get(index);
            // 调用服务删除消息
            boolean success = aiChatService.deleteMessage(message.getId());
            
            return success ? AjaxResult.success() : AjaxResult.error("删除失败");
        } catch (ServiceException e) {
            // 处理服务异常
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            // 处理其他异常
            log.error("删除消息失败: {}", e.getMessage(), e);
            return AjaxResult.error("删除失败，请稍后重试");
        }
    }

    /**
     * 测试接口 - 用于调试
     */
    @GetMapping("/test")
    public AjaxResult test() {
        String testContent = "这是一个测试消息";
        log.info("测试接口返回: {}", testContent);
        return AjaxResult.success(testContent);
    }

    /**
     * 创建会话请求DTO
     */
    @lombok.Data
    public static class CreateSessionRequest {
        private String title;
    }

    /**
     * 对话请求DTO
     */
    @lombok.Data
    public static class ChatRequest {
        private String message;
        private String sessionId;
    }
}
