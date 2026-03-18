package com.shejiao.web.service;

import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.NoteSearchVO;

import java.util.List;

/**
 * 推荐服务接口
 * 实现协同过滤推荐算法
 *
 * @Author shejiao
 */
public interface IWebRecommendService {

    /**
     * 根据用户ID获取推荐用户列表
     *
     * @param userId 用户ID
     * @return 推荐用户列表
     */
    List<WebUser> getRecommendUser(Long userId);

    /**
     * 根据用户ID获取推荐笔记列表
     *
     * @param userId 用户ID
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> getRecommendNote(Long userId);

    /**
     * 根据用户ID分页获取推荐笔记列表
     * 支持无限滚动，每次请求返回新的推荐内容
     *
     * @param userId 用户ID
     * @param currentPage 当前页
     * @param pageSize 每页大小
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> getRecommendNoteByPage(Long userId, long currentPage, long pageSize);

    /**
     * 用户协同过滤推荐算法
     *
     * @param userId 用户ID
     * @param k      相似用户数量
     * @param n      推荐数量
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> userBasedCF(Long userId, int k, int n);

    /**
     * 物品协同过滤推荐算法
     *
     * @param userId 用户ID
     * @param k      相似物品数量
     * @param n      推荐数量
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> itemBasedCF(Long userId, int k, int n);

    /**
     * 混合协同过滤推荐算法
     *
     * @param userId 用户ID
     * @param k1     相似用户数量
     * @param k2     相似物品数量
     * @param n      推荐数量
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> hybridCF(Long userId, int k1, int k2, int n);
}
