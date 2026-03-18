package com.shejiao.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.utils.poi.ExcelUtil;
import com.shejiao.web.domain.entity.AiChatMessage;
import com.shejiao.web.domain.entity.AiChatSession;
import com.shejiao.web.service.IAiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * AI对话管理控制器（用于管理系统）
 */
@RestController
@RequestMapping({"/system/ai", "/web/ai"})
@RequiredArgsConstructor
public class AiChatManageController extends BaseController {

    private final IAiChatService aiChatService;

    /**
     * 获取聊天会话列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:list')")
    @GetMapping({"/chat/list", "/list"})
    public AjaxResult getChatList(@RequestParam(defaultValue = "1") long currentPage,
                                  @RequestParam(defaultValue = "10") long pageSize,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String title) {
        List<AiChatSession> sessions = aiChatService.getAllSessions(username, title);
        
        // 手动分页
        int total = sessions.size();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        List<AiChatSession> pageSessions = new ArrayList<>();
        if (start < total) {
            pageSessions = sessions.subList(start, end);
        }
        
        Page<AiChatSession> page = new Page<>(currentPage, pageSize);
        page.setRecords(pageSessions);
        page.setTotal(total);
        
        // 为了兼容前端代码，返回rows和total格式
        AjaxResult result = AjaxResult.success();
        result.put("rows", pageSessions);
        result.put("total", total);
        return result;
    }

    /**
     * 获取消息列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('system:ai:message:list')")
    @GetMapping("/message/list")
    public AjaxResult getMessageList(@RequestParam(defaultValue = "1") long currentPage,
                                     @RequestParam(defaultValue = "10") long pageSize,
                                     @RequestParam(required = false) String sessionId,
                                     @RequestParam(required = false) String role) {
        List<AiChatMessage> messages = aiChatService.getAllMessages(sessionId, role);
        
        // 手动分页
        int total = messages.size();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        List<AiChatMessage> pageMessages = new ArrayList<>();
        if (start < total) {
            pageMessages = messages.subList(start, end);
        }
        
        Page<AiChatMessage> page = new Page<>(currentPage, pageSize);
        page.setRecords(pageMessages);
        page.setTotal(total);
        return AjaxResult.success(page);
    }

    /**
     * 获取聊天会话详情
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:list')")
    @GetMapping("/{id}")
    public AjaxResult getChatDetail(@PathVariable String id) {
        AiChatSession session = aiChatService.getSession(id);
        if (session == null) {
            return AjaxResult.error("会话不存在");
        }
        return AjaxResult.success(session);
    }

    /**
     * 刷新AI对话数据
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:edit')")
    @PostMapping("/refresh")
    public AjaxResult refreshAiData() {
        // 这里可以添加刷新逻辑，比如重新索引到ES等
        return AjaxResult.success();
    }

    /**
     * 删除聊天会话
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:delete')")
    @DeleteMapping("/{sessionId}")
    public AjaxResult deleteChat(@PathVariable String sessionId) {
        boolean success = aiChatService.deleteSession(sessionId);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除聊天会话
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:delete')")
    @DeleteMapping("/chat/batch")
    public AjaxResult batchDeleteChat(@RequestBody List<String> sessionIds) {
        boolean success = aiChatService.batchDeleteSessions(sessionIds);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 删除消息
     */
    @PreAuthorize("@ss.hasPermi('system:ai:message:delete')")
    @DeleteMapping("/message/{messageId}")
    public AjaxResult deleteMessage(@PathVariable String messageId) {
        boolean success = aiChatService.deleteMessage(messageId);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除消息
     */
    @PreAuthorize("@ss.hasPermi('system:ai:message:delete')")
    @DeleteMapping("/message/batch")
    public AjaxResult batchDeleteMessage(@RequestBody List<String> messageIds) {
        boolean success = aiChatService.batchDeleteMessages(messageIds);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 导出聊天会话列表
     */
    @PreAuthorize("@ss.hasPermi('system:ai:chat:export')")
    @PostMapping("/chat/export")
    public void exportChatList(HttpServletResponse response, 
                              @RequestParam(required = false) String username, 
                              @RequestParam(required = false) String title) {
        List<AiChatSession> sessions = aiChatService.getAllSessions(username, title);
        ExcelUtil<AiChatSession> util = new ExcelUtil<>(AiChatSession.class);
        util.exportExcel(response, sessions, "AI聊天会话数据");
    }

    /**
     * 导出消息列表
     */
    @PreAuthorize("@ss.hasPermi('system:ai:message:export')")
    @PostMapping("/message/export")
    public void exportMessageList(HttpServletResponse response, 
                                @RequestParam(required = false) String sessionId, 
                                @RequestParam(required = false) String role) {
        List<AiChatMessage> messages = aiChatService.getAllMessages(sessionId, role);
        ExcelUtil<AiChatMessage> util = new ExcelUtil<>(AiChatMessage.class);
        util.exportExcel(response, messages, "AI聊天消息数据");
    }
}
