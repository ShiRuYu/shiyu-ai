package com.shiyu.ai.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeChunkMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChunkRepository {

    private final KnowledgeChunkMapper mapper;

    public KnowledgeChunkRepository(KnowledgeChunkMapper mapper) {
        this.mapper = mapper;
    }

    public void insert(KnowledgeChunkDO chunk) {
        mapper.insert(chunk);
    }

    public void insertBatch(List<KnowledgeChunkDO> chunks) {
        mapper.insertBatch(chunks);
    }

    public KnowledgeChunkDO getById(Long id) {
        return mapper.selectOneById(id);
    }

    public KnowledgeChunkDO getByDocumentIdAndIndex(Long documentId, Integer chunkIndex) {
        return mapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("document_id", documentId)
                        .eq("chunk_index", chunkIndex));
    }

    public List<KnowledgeChunkDO> getByDocumentId(Long documentId) {
        return mapper.selectListByQuery(
                QueryWrapper.create().eq("document_id", documentId));
    }

    public void deleteByDocumentId(Long documentId) {
        mapper.deleteByQuery(
                QueryWrapper.create().eq("document_id", documentId));
    }

    public List<KnowledgeChunkDO> findAll() {
        return mapper.selectAll();
    }

    public long count() {
        return mapper.selectCountByQuery(new QueryWrapper());
    }
}
