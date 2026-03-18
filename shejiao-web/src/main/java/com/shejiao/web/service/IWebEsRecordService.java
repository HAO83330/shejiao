package com.shejiao.web.service;

import com.shejiao.web.domain.dto.EsRecordDTO;
import com.shejiao.web.domain.vo.RecordSearchVO;

import java.util.List;

/**
 * ES
 *
 * @Author shejiao
 */
public interface IWebEsRecordService {

    /**
     * 获取搜索记录
     */
    List<RecordSearchVO> getRecordByKeyWord(EsRecordDTO esRecordDTO);

    /**
     * 热门关键词
     */
    List<RecordSearchVO> getHotRecord();

    /**
     * 增加搜索记录
     */
    void addRecord(EsRecordDTO esRecordDTO);

    /**
     * 删除搜索记录
     */
    void clearRecordByUser(EsRecordDTO esRecordDTO);

    /**
     * 清空搜索记录
     */
    void clearAllRecord();

    /**
     * 获取个性化推荐搜索词（猜你想搜）
     * 基于用户历史搜索、浏览记录和热搜
     */
    List<RecordSearchVO> getGuessYouWant(String uid);
}
