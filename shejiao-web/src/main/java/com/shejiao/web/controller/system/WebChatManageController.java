package com.shejiao.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.utils.poi.ExcelUtil;
import com.shejiao.web.domain.entity.WebChat;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.WebChatExportVO;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.service.IWebChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户聊天管理控制器（用于管理系统）
 */
@RestController
@RequestMapping({"/system/chat", "/web/chat"})
@RequiredArgsConstructor
public class WebChatManageController extends BaseController {

    private final IWebChatService webChatService;
    private final WebUserMapper webUserMapper;

    /**
     * 获取用户聊天列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('system:chat:list')")
    @GetMapping({"/list"})
    public AjaxResult getUserChatList(@RequestParam(defaultValue = "1") long currentPage,
                                     @RequestParam(defaultValue = "10") long pageSize,
                                     @RequestParam(required = false) String sessionId,
                                     @RequestParam(required = false) String senderId,
                                     @RequestParam(required = false) String receiverId) {
        // 使用MyBatis-Plus的分页查询
        Page<WebChat> page = new Page<>(currentPage, pageSize);
        
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WebChat> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (senderId != null && !senderId.isEmpty() && receiverId != null && !receiverId.isEmpty()) {
            // 查询两个用户之间的所有聊天记录
            queryWrapper.and(qw -> {
                qw.eq("send_uid", senderId).eq("accept_uid", receiverId)
                  .or().eq("send_uid", receiverId).eq("accept_uid", senderId);
            });
        } else if (senderId != null && !senderId.isEmpty()) {
            queryWrapper.eq("send_uid", senderId);
        } else if (receiverId != null && !receiverId.isEmpty()) {
            queryWrapper.eq("accept_uid", receiverId);
        }
        
        // 按时间戳降序排序，确保最新的聊天记录显示在前面
        queryWrapper.orderByDesc("timestamp");
        
        // 执行分页查询
        Page<WebChat> resultPage = webChatService.page(page, queryWrapper);
        
        // 为了兼容前端代码，返回rows和total格式
        AjaxResult result = AjaxResult.success();
        result.put("rows", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        return result;
    }

    /**
     * 删除用户聊天
     */
    @PreAuthorize("@ss.hasPermi('system:chat:delete')")
    @DeleteMapping("/{id}")
    public AjaxResult deleteUserChat(@PathVariable String id) {
        boolean success = webChatService.removeById(id);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 批量删除用户聊天
     */
    @PreAuthorize("@ss.hasPermi('system:chat:delete')")
    @DeleteMapping("/batch")
    public AjaxResult batchDeleteUserChat(@RequestBody List<String> ids) {
        boolean success = webChatService.removeByIds(ids);
        return success ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 导出用户聊天列表
     */
    @PreAuthorize("@ss.hasPermi('system:chat:export')")
    @PostMapping("/export")
    public void exportUserChatList(HttpServletResponse response, 
                                  @RequestParam(required = false) String sessionId, 
                                  @RequestParam(required = false) String senderId, 
                                  @RequestParam(required = false) String receiverId) {
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WebChat> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (senderId != null && !senderId.isEmpty() && receiverId != null && !receiverId.isEmpty()) {
            // 查询两个用户之间的所有聊天记录
            queryWrapper.and(qw -> {
                qw.eq("send_uid", senderId).eq("accept_uid", receiverId)
                  .or().eq("send_uid", receiverId).eq("accept_uid", senderId);
            });
        } else if (senderId != null && !senderId.isEmpty()) {
            queryWrapper.eq("send_uid", senderId);
        } else if (receiverId != null && !receiverId.isEmpty()) {
            queryWrapper.eq("accept_uid", receiverId);
        }
        
        // 按时间戳降序排序
        queryWrapper.orderByDesc("timestamp");
        
        // 获取所有符合条件的聊天记录
        List<WebChat> chats = webChatService.list(queryWrapper);
        
        // 收集所有用户ID
        Set<String> userIds = chats.stream()
                .flatMap(chat -> Arrays.asList(chat.getSendUid(), chat.getAcceptUid()).stream())
                .collect(Collectors.toSet());
        
        // 批量获取用户信息
        List<WebUser> users = webUserMapper.selectBatchIds(userIds);
        Map<String, WebUser> userMap = users.stream()
                .collect(Collectors.toMap(WebUser::getId, user -> user));
        
        // 转换为导出VO
        List<WebChatExportVO> exportVOs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (WebChat chat : chats) {
            WebChatExportVO vo = new WebChatExportVO();
            vo.setSendUid(chat.getSendUid());
            vo.setAcceptUid(chat.getAcceptUid());
            
            // 设置发送方名称
            WebUser sender = userMap.get(chat.getSendUid());
            vo.setSenderName(sender != null ? sender.getUsername() : chat.getSendUid());
            
            // 设置接收方名称
            WebUser receiver = userMap.get(chat.getAcceptUid());
            vo.setReceiverName(receiver != null ? receiver.getUsername() : chat.getAcceptUid());
            
            // 设置消息内容
            if (chat.getContent() != null) {
                // 对于图片类型的消息，只设置图片内容，不显示URL
                if (chat.getMsgType() == 2) {
                    // 设置图片内容，ExcelUtil会自动将其导出为图片
                    vo.setImageContent(chat.getContent());
                    // 设置内容为简单描述，不显示URL
                    vo.setContent("[图片]");
                } else {
                    // 对于其他类型的消息，截断内容
                    if (chat.getContent().length() > 32767) {
                        vo.setContent(chat.getContent().substring(0, 32767) + "...");
                    } else {
                        vo.setContent(chat.getContent());
                    }
                }
            } else {
                vo.setContent("");
            }
            
            vo.setChatType(chat.getChatType());
            vo.setMsgType(chat.getMsgType());
            vo.setTimestamp(chat.getTimestamp());
            
            // 设置创建时间
            if (chat.getCreateTime() != null) {
                vo.setCreateTime(sdf.format(chat.getCreateTime()));
            }
            
            exportVOs.add(vo);
        }
        
        ExcelUtil<WebChatExportVO> util = new ExcelUtil<>(WebChatExportVO.class);
        util.exportExcel(response, exportVOs, "用户聊天数据");
    }
}
