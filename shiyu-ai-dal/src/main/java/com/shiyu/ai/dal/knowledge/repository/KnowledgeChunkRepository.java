package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeChunkDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeChunkMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChunkRepository {

    private final KnowledgeChunkMapper mapper;

    public KnowledgeChunkRepository(KnowledgeChunkMapper mapper) {
        this.mapper = mapper;
    }

    public void insert(KnowledgeChunkBO bo) {
        KnowledgeChunkDO dataObject = MapstructUtils.convert(bo, KnowledgeChunkDO.class);
        mapper.insert(dataObject);
        bo.setId(dataObject.getId());
    }

    public void insertBatch(List<KnowledgeChunkBO> boList) {
        mapper.insertBatch(MapstructUtils.convert(boList, KnowledgeChunkDO.class));
    }

    public KnowledgeChunkBO getById(Long id) {
        KnowledgeChunkDO d = mapper.selectOneById(id);
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    public KnowledgeChunkBO getByDocumentIdAndIndex(Long documentId, Integer chunkIndex) {
        KnowledgeChunkDO d = mapper.selectOneByQuery(
                QueryWrapper.create().eq("document_id", documentId).eq("chunk_index", chunkIndex));
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    public List<KnowledgeChunkBO> getByDocumentId(Long documentId) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("document_id", documentId)), KnowledgeChunkBO.class);
    }

    public void deleteByDocumentId(Long documentId) {
        mapper.deleteByQuery(QueryWrapper.create().eq("document_id", documentId));
    }

    public List<KnowledgeChunkBO> findAll() {
        return MapstructUtils.convert(mapper.selectAll(), KnowledgeChunkBO.class);
    }

    public long count() {
        return mapper.selectCountByQuery(new QueryWrapper());
    }

    public List<KnowledgeChunkBO> findBySpace(Long spaceId) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeChunkDO::getSpaceId, spaceId)
                        .orderBy(KnowledgeChunkDO::getDocumentId, true)
                        .orderBy(KnowledgeChunkDO::getChunkIndex, true)), KnowledgeChunkBO.class);
    }

    public void assignDefaultSpace(Long spaceId) {
        List<KnowledgeChunkDO> records = mapper.selectListByQuery(
                QueryWrapper.create().isNull(KnowledgeChunkDO::getSpaceId));
        for (KnowledgeChunkDO record : records) {
            record.setSpaceId(spaceId);
            mapper.update(record);
        }
    }
}
