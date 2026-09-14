package com.shiyu.ai.knowledge.implementation.application.rag;

import java.util.List;

/**
 * ChunkSplitter 接口，定义知识模块的能力边界。
 */
public interface ChunkSplitter {

    /**
     * 执行 {@code split} 定义的接口操作。
     *
     * @param text 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Chunk> split(String text);

    /**
     * {@code Chunk} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param content 内容，表示该记录组件承载的数据。
     * @param index index 属性，表示该记录组件承载的数据。
     * @param startPos startPos 属性，表示该记录组件承载的数据。
     * @param endPos endPos 属性，表示该记录组件承载的数据。
     */
    record Chunk(String content, int index, int startPos, int endPos) {}
}
