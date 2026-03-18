package com.shejiao.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.web.domain.entity.WebTag;
import com.shejiao.web.domain.vo.NoteVO;
import com.shejiao.web.domain.vo.TagVO;
import com.shejiao.web.mapper.WebTagMapper;
import com.shejiao.web.service.IWebTagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签
 *
 * @Author shejiao
 */
@Service
public class WebTagServiceImpl extends ServiceImpl<WebTagMapper, WebTag> implements IWebTagService {


    /**
     * 获取热门标签
     */
    @Override
    public List<TagVO> getHotTagList() {
        return null;
    }

    /**
     * 根据标签ID获取图片信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param tagId       标签id
     * @param type        类型
     */
    @Override
    public Page<NoteVO> getNoteByTagId(long currentPage, long pageSize, String tagId, Integer type) {
        return null;
    }

    /**
     * 根据关键词获取标签
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param keyword     关键词
     */
    @Override
    public Page<WebTag> getTagByKeyword(long currentPage, long pageSize, String keyword) {
        QueryWrapper<WebTag> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like("title", keyword);
        }
        queryWrapper.orderByDesc("like_count");
        return this.page(new Page<WebTag>((int) currentPage, (int) pageSize), queryWrapper);
    }
}
