package com.shejiao.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shejiao.web.domain.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI对话消息Mapper
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {

    /**
     * 根据会话ID查询消息列表，按消息顺序排序
     */
    @Select("SELECT * FROM ai_chat_message WHERE session_id = #{sessionId} ORDER BY message_order ASC")
    List<AiChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 获取会话的最大消息顺序
     */
    @Select("SELECT COALESCE(MAX(message_order), 0) FROM ai_chat_message WHERE session_id = #{sessionId}")
    Integer selectMaxOrderBySessionId(@Param("sessionId") String sessionId);
}
