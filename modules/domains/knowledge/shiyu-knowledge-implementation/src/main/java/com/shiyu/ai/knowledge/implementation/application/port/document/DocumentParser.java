package com.shiyu.ai.knowledge.implementation.application.port.document;

import java.nio.charset.StandardCharsets;

/**
 * 解析或编解码 文档 相关的外部内容和领域数据。
 */
public interface DocumentParser {

    /**
     * 查询 文档 相关业务数据，并返回处理结果。
     *
     * @return 返回 文档 相关操作生成的结果数据。
     */
    String getSupportedFormat();

    /**
     * 执行 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 文档 相关操作生成的结果数据。
     */
    ParseResult parse(String content);

    /**
     * 执行 文档 相关业务数据，并返回处理结果。
     *
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 文档 相关操作生成的结果数据。
     */
    default ParseResult parse(byte[] content) {
        return parse(new String(content, StandardCharsets.UTF_8));
    }

    /**
     * 封装 Parse 相关的不可变数据及其字段约束。
     */
    record ParseResult(String title, String text, String metadata) {}
}
