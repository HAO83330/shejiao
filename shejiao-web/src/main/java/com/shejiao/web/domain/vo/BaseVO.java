package com.shejiao.web.domain.vo;

import com.shejiao.common.annotation.IdValid;
import com.shejiao.common.validator.group.Delete;
import com.shejiao.common.validator.group.Update;
import com.shejiao.web.domain.PageInfo;
import lombok.Data;

/**
 * BaseVO   view object 表现层 基类对象
 *
 * @Author shejiao
 * @create: 2019-12-03-22:38
 */
@Data
public class BaseVO<T> extends PageInfo<T> {

    /**
     * 唯一UID
     */
    @IdValid(groups = {Update.class, Delete.class})
    private String uid;

    private Integer status;
}
