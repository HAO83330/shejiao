package com.shejiao.common.constant;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 消息通知数量设置
 *
 * @Author shejiao
 */
@ApiModel("消息通知数量")
public interface ImConstant {

    @ApiModelProperty("消息通知数量")
    String MESSAGE_COUNT_KEY = "messageCountKey:";
}
