package com.shiyu.ai.conversation;

import java.util.regex.Pattern;

/** Central redaction/estimation policy used by HTTP and generation logging. */
public final class PromptSafety {
    private static final Pattern SECRET = Pattern.compile("(?i)(authorization|api[-_ ]?key|cookie|tool[-_ ]?key)\\s*[:=]\\s*(?:Bearer\\s+)?[^,;\\s]+|Bearer\\s+[A-Za-z0-9._~+/=-]+", Pattern.MULTILINE);
    private PromptSafety() {}
    public static String redact(String value) { return value == null ? null : SECRET.matcher(value).replaceAll("[REDACTED]"); }
    public static long estimateTokens(String value) { return value == null || value.isBlank() ? 0 : Math.max(1, value.codePointCount(0, value.length()) / 4); }
}
