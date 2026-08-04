package com.shiyu.ai.common.core.xss;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 自定义 XSS 校验注解实现
 * 使用 Jsoup 白名单方式净化并比对，比正则更全面
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

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
