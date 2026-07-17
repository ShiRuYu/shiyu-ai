package com.shiyu.ai.vector;

import java.util.List;

/**
 * 向量存储 SPI — 统一接口
 * 支持 JVector / PGVector / InMemory 等多种实现
 */
public interface VectorStore {

    /**
     * 返回向量存储类型标识（如 "inmemory", "jvector", "pgvector"）
     */
    String type();
 
    /**
     * 插入或更新向量记录
     */
    void upsert(VectorRecord record);

    /**
     * 批量插入或更新
     */
    default void upsertBatch(List<VectorRecord> records) {
        for (VectorRecord r : records) {
            upsert(r);
        }
    }

    /**
     * 搜索（简化版）
     */
    List<VectorRecord> search(float[] queryVector, int topK);

    /**
     * 搜索（高级版 — 支持过滤、最小分数、搜索类型）
     */
    default List<VectorRecord> search(VectorSearchRequest request) {
        return search(request.getQueryVector(), request.getTopK());
    }

    /**
     * 删除记录
     */
    void delete(String id);

    /**
     * 批量删除
     */
    default void deleteBatch(List<String> ids) {
        for (String id : ids) {
            delete(id);
        }
    }

    /**
     * 重建索引
     */
    default void rebuild() {
    }

    /**
     * 记录数
     */
    default int size() {
        return 0;
    }
}
