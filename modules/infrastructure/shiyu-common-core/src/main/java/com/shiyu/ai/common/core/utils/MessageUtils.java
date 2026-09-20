package com.shiyu.ai.common.core.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 提供 消息 相关的通用辅助操作，供业务和基础设施复用。
 */
public class MessageUtils {

    /**
     * 来源，表示当前对象中的对应属性。
     */
    private static volatile MessageSource MESSAGE_SOURCE;

    private static MessageSource getMessageSource() {
        if (MESSAGE_SOURCE == null) {
            synchronized (MessageUtils.class) {
                if (MESSAGE_SOURCE == null) {
                    MESSAGE_SOURCE = SpringUtils.getBean(MessageSource.class);
                }
            }
        }
        return MESSAGE_SOURCE;
    }

    /**
     * 执行 消息 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param args 用于完成本次业务处理的 args 参数。
     * @return 返回 消息 相关操作生成的结果数据。
     */
    public static String message(String code, Object... args) {
        return getMessageSource().getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
