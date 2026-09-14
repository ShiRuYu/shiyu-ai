package com.shiyu.ai.conversation.implementation.application;

import java.util.regex.Pattern;

/**
 * 执行提示词内容安全校验和策略过滤。
 */
public final class PromptSafety {
    private static final Pattern SECRET =
            Pattern.compile(
                    "(?i)(authorization|api[-_ ]?key|cookie|tool[-_"
                        + " ]?key)\\s*[:=]\\s*(?:Bearer\\s+)?[^,;\\s]+|Bearer\\s+[A-Za-z0-9._~+/=-]+",
                    Pattern.MULTILINE);

    private PromptSafety() {}

    /**
     * {@code redact} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String redact(String value) {
        return value == null ? null : SECRET.matcher(value).replaceAll("[REDACTED]");
    }

    /**
     * {@code estimateTokens} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static long estimateTokens(String value) {
        return value == null || value.isBlank()
                ? 0
                : Math.max(1, value.codePointCount(0, value.length()) / 4);
    }
}
