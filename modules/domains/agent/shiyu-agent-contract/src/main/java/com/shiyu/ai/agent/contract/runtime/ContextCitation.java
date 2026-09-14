package com.shiyu.ai.agent.contract.runtime;

/**
 * {@code ContextCitation} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param title 标题，表示该记录组件承载的数据。
 * @param uri 资源地址，表示该记录组件承载的数据。
 * @param locator locator 属性，表示该记录组件承载的数据。
 * @param checksum checksum 属性，表示该记录组件承载的数据。
 */
public record ContextCitation(String title, String uri, String locator, String checksum) {}
