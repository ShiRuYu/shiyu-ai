package com.shiyu.ai.knowledge.implementation.application.port.rag;

import java.util.List;

/**
 * 定义 Chunk Splitter 相关的协作契约和调用边界。
 */
public interface ChunkSplitter {

    /**
     * 执行 Chunk Splitter 相关业务数据，并返回处理结果。
     *
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Chunk> split(String text);

    /**
     * 封装 Chunk 相关的不可变数据及其字段约束。
     */
    record Chunk(String content, int index, int startPos, int endPos) {}
}
