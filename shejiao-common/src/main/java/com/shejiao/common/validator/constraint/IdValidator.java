package com.shejiao.common.validator.constraint;

import com.shejiao.common.annotation.IdValid;
import com.shejiao.common.constant.Constantss;
import com.shejiao.common.utils.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * ID校验器，主要判断是否为空，并且长度是否为32
 *
 * @Author shejiao
 */
public class IdValidator implements ConstraintValidator<IdValid, String> {


    @Override
    public void initialize(IdValid constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value) || StringUtils.isEmpty(value.trim()) || value.length() != Constantss.THIRTY_TWO) {
            return false;
        }
        return true;
    }
}
