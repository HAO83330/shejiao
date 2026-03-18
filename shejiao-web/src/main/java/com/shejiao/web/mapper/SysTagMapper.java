package com.shejiao.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shejiao.web.domain.entity.WebTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签信息 数据层
 *
 * @Author shejiao
 */
@Mapper
public interface SysTagMapper extends BaseMapper<WebTag> {

}
