package com.shejiao.web.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * ES-笔记
 *
 * @Author shejiao
 */
@Data
public class EsRecordDTO implements Serializable {

    private String keyword;

    private String uid;

}
