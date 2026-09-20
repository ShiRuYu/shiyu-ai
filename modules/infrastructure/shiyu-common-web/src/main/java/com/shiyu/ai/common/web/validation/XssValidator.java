package com.shiyu.ai.common.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 校验或约束 Xss 相关的请求、状态和访问规则。
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    /**
     * 校验或判断 Xss 相关业务数据，并返回处理结果。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     * @param constraintValidatorContext 用于完成本次业务处理的 constraintValidatorContext 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        // 使用 Jsoup 白名单净化后比对：若前后不一致则说明含有非法标签
        String cleaned = Jsoup.clean(value, Safelist.basic());
        return cleaned.equals(value);
    }
}
