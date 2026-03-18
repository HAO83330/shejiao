package com.shejiao.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shejiao.web.domain.dto.BrowseRecordDTO;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.vo.NoteSearchVO;

import java.util.List;

/**
 * 浏览记录
 *
 * @Author shejiao
 */
public interface IWebBrowseRecordService extends IService<WebNote> {

    /**
     * 获取浏览记录
     */
    List<NoteSearchVO> getAllBrowseRecordByUser(long page, long limit, String uid);

    /**
     * 添加浏览记录
     */
    void addBrowseRecord(BrowseRecordDTO browseRecordDTO);

    /**
     * 删除浏览记录
     */
    void delRecord(String uid, List<String> idList);
}
