package com.shejiao.web.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * ES-笔记
 *
 * @Author shejiao
 */
@Data
public class EsNoteDTO implements Serializable {

    private String keyword;

    // 0是指全部 1是指点赞排序 2是指时间排序
    private Integer type;

    private String cid;

    private String cpid;
}
