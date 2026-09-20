package com.shiyu.ai.agent.contract.runtime;

/**
 * 封装 Context Citation 相关的不可变数据及其字段约束。
 */
public record ContextCitation(String title, String uri, String locator, String checksum) {}
