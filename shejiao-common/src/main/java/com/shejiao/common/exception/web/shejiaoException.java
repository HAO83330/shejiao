package com.shejiao.common.exception.web;

import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.common.utils.MessageUtil;
import lombok.Data;

/**
 * 异常
 *
 * @Author shejiao
 */
@Data
public class shejiaoException extends RuntimeException {

    // 异常状态码
    private Integer code;


    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param message
     * @param code
     */
    public shejiaoException(String message, Integer code) {
        super(message);
        this.code = code;
    }


    public shejiaoException(int code, String... params) {
        super(MessageUtil.getMessage(code, params));
        this.code = code;
    }

    public shejiaoException(String msg) {
        super(msg);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    /**
     * 接收枚举类型对象
     *
     * @param resultCodeEnum
     */
    public shejiaoException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
