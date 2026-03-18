package com.shejiao.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shejiao.web.domain.entity.WebComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 数据层
 *
 * @Author shejiao
 */
@Mapper
public interface SysCommentMapper extends BaseMapper<WebComment> {

}
