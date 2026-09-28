package com.shiyu.ai.runtimeconsole.config;

import java.util.Map;

/** 封装运行时配置编辑、版本校验及密钥变更内容。 */
public record ConfigChangeSet(
        long expectedVersion,
        Map<String, String> values,
        Map<String, SecretChange> secrets) {

    /** 描述单项密钥配置的编辑动作和新值。 */
    public record SecretChange(String action, String value) {}
}
