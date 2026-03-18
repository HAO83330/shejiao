package com.shejiao.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shejiao.web.domain.entity.WebMention;
import com.shejiao.web.domain.vo.MentionVO;

import java.util.List;

/**
 * @Author shejiao
 */
public interface IWebMentionService extends IService<WebMention> {
    /**
     * 获取当前用户的@提及通知列表
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @return 提及通知列表
     */
    List<MentionVO> getNoticeMention(long currentPage, long pageSize);
}
