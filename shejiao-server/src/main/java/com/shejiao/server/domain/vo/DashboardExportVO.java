package com.shejiao.server.domain.vo;

import com.shejiao.common.annotation.Excel;
import lombok.Data;

/**
 * 首页数据导出VO
 *
 * @Author shejiao
 */
@Data
public class DashboardExportVO {

    /**
     * 数据类型
     */
    @Excel(name = "数据类型", sort = 1)
    private String dataType;

    /**
     * 数据名称
     */
    @Excel(name = "数据名称", sort = 2)
    private String dataName;

    /**
     * 数据值
     */
    @Excel(name = "数据值", sort = 3)
    private String dataValue;

    /**
     * 单位
     */
    @Excel(name = "单位", sort = 4)
    private String unit;

    /**
     * 描述
     */
    @Excel(name = "描述", sort = 5)
    private String description;
}
