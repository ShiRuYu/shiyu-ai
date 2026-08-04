package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import java.util.List;

public interface KnowledgeChunkRepository {
    void insert(KnowledgeChunkBO bo);
    void insertBatch(List<KnowledgeChunkBO> boList);
    KnowledgeChunkBO getById(Long id);
    KnowledgeChunkBO getByDocumentIdAndIndex(Long documentId, Integer chunkIndex);
    List<KnowledgeChunkBO> getByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
    List<KnowledgeChunkBO> findAll();
    long count();
    List<KnowledgeChunkBO> findBySpace(Long spaceId);
    void assignDefaultSpace(Long spaceId);
}
