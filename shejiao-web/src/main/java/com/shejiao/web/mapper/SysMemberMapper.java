package com.shejiao.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shejiao.web.domain.entity.WebUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员信息 数据层
 *
 * @Author shejiao
 */
@Mapper
public interface SysMemberMapper extends BaseMapper<WebUser> {

}
