package com.shejiao.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shejiao.web.domain.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI会话Mapper
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {

    /**
     * 根据用户ID查询会话列表
     */
    @Select("SELECT * FROM ai_chat_session WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<AiChatSession> selectByUserId(@Param("userId") String userId);
}
