package com.shejiao.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shejiao.web.domain.dto.EsNoteDTO;
import com.shejiao.web.domain.entity.WebNavbar;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.NoteSearchVO;

import java.util.List;

/**
 * ES
 *
 * @Author shejiao
 */
public interface IWebEsNoteService extends IService<WebNote> {

    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param esNoteDTO   笔记
     */
    Page<NoteSearchVO> getNoteByDTO(long currentPage, long pageSize, EsNoteDTO esNoteDTO);

    /**
     * 搜索对应的笔记
     *
     * @param esNoteDTO 笔记
     */
    List<WebNavbar> getCategoryAgg(EsNoteDTO esNoteDTO);

    /**
     * 分页查询笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVO> getRecommendNote(long currentPage, long pageSize);

    /**
     * 批量获取推荐笔记（用于前端缓存）
     * 一次返回100条推荐数据
     *
     * @return 推荐笔记列表
     */
    List<NoteSearchVO> getRecommendNoteBatch();

    /**
     * 获取推荐用户
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<WebUser> getRecommendUser(long currentPage, long pageSize);

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVO> getHotNote(long currentPage, long pageSize);

    /**
     * 增加笔记
     *
     * @param noteSearchVo 笔记
     */
    void addNote(NoteSearchVO noteSearchVo);

    /**
     * 修改笔记
     *
     * @param noteSearchVo 笔记
     */
    void updateNote(NoteSearchVO noteSearchVo);

    /**
     * 批量增加笔记
     */
    void addNoteBulkData();

    /**
     * 删除es中的笔记
     *
     * @param noteId 笔记 ID
     */
    void deleteNote(String noteId);

    /**
     * 清空笔记
     */
    void delNoteBulkData();

    /**
     * 重置
     */
    void refreshNoteData();
}
