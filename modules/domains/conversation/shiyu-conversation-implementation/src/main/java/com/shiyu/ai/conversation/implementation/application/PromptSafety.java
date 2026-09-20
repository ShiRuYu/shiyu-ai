package com.shiyu.ai.conversation.implementation.application;

import java.util.regex.Pattern;

/**
 * 编排 提示词 Safety 所属应用流程的输入、协作和业务结果。
 */
public final class PromptSafety {
    private static final Pattern SECRET =
            Pattern.compile(
                    "(?i)(authorization|api[-_ ]?key|cookie|tool[-_"
                        + " ]?key)\\s*[:=]\\s*(?:Bearer\\s+)?[^,;\\s]+|Bearer\\s+[A-Za-z0-9._~+/=-]+",
                    Pattern.MULTILINE);

    private PromptSafety() {}

    /**
     * 执行 提示词 Safety 相关业务数据，并返回处理结果。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     * @return 返回 提示词 Safety 相关操作生成的结果数据。
     */
    public static String redact(String value) {
        return value == null ? null : SECRET.matcher(value).replaceAll("[REDACTED]");
    }

    /**
     * 执行 提示词 Safety 相关业务数据，并返回处理结果。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     * @return 返回 提示词 Safety 相关操作生成的结果数据。
     */
    public static long estimateTokens(String value) {
        return value == null || value.isBlank()
                ? 0
                : Math.max(1, value.codePointCount(0, value.length()) / 4);
    }
}
