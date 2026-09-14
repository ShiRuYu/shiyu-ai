package com.shiyu.ai.knowledge.implementation.application.document;

import java.nio.charset.StandardCharsets;

/** 文档解析器 SPI 支持多种文档格式解析 */
public interface DocumentParser {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 操作结果。
     */
    String getSupportedFormat();

    /**
     * 执行 {@code parse} 定义的接口操作。
     *
     * @param content 方法参数。
     *
     * @return 操作结果。
     */
    ParseResult parse(String content);

    /**
     * 执行 {@code parse} 定义的接口操作。
     *
     * @param content 方法参数。
     *
     * @return 操作结果。
     */
    default ParseResult parse(byte[] content) {
        return parse(new String(content, StandardCharsets.UTF_8));
    }

    /**
     * {@code ParseResult} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param title 标题，表示该记录组件承载的数据。
     * @param text text 属性，表示该记录组件承载的数据。
     * @param metadata 元数据，表示该记录组件承载的数据。
     */
    record ParseResult(String title, String text, String metadata) {}
}
