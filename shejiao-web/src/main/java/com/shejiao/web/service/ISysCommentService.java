package com.shejiao.web.service;

import com.shejiao.common.core.domain.Query;
import com.shejiao.web.domain.entity.WebComment;
import com.shejiao.web.domain.vo.CommentVO;

import java.util.List;

/**
 * 评论信息 服务层
 *
 * @Author shejiao
 */
public interface ISysCommentService {

    /**
     * 获取所有一级分类列表
     *
     * @param query 评论信息
     */
    List<CommentVO> selectCommentList(Query query);

    /**
     * 查询一级以下评论
     *
     * @param query 评论信息
     */
    List<CommentVO> selectTreeList(Query query);

    /**
     * 通过评论ID查询评论信息
     *
     * @param id 评论ID
     */
    WebComment selectCommentById(Long id);

    /**
     * 通过笔记ID查询评论信息
     *
     * @param nid 笔记ID
     */
    List<WebComment> selectCommentByNid(Long nid);

    /**
     * 批量删除评论信息
     *
     * @param ids 需要删除的评论ID
     */
    int deleteCommentByIds(Long[] ids);

    Object getCommentCount(int enable);
    
    /**
     * 获取指定月份评论数量
     */
    Object getCommentCount(int enable, String month);
}
