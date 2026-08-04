package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeChunkDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeChunkMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChunkRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository {

    private final KnowledgeChunkMapper mapper;

    public KnowledgeChunkRepositoryImpl(KnowledgeChunkMapper mapper) {
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
        KnowledgeChunkDO d = mapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeChunkDO::getId, id)
                .eq(KnowledgeChunkDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    public KnowledgeChunkBO getByDocumentIdAndIndex(Long documentId, Integer chunkIndex) {
        KnowledgeChunkDO d = mapper.selectOneByQuery(
                QueryWrapper.create().eq("document_id", documentId).eq("chunk_index", chunkIndex)
                        .eq(KnowledgeChunkDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    public List<KnowledgeChunkBO> getByDocumentId(Long documentId) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("document_id", documentId)
                        .eq(KnowledgeChunkDO::getDelFlag, 0)), KnowledgeChunkBO.class);
    }

    public void deleteByDocumentId(Long documentId) {
        mapper.deleteByQuery(QueryWrapper.create().eq("document_id", documentId));
    }

    public List<KnowledgeChunkBO> findAll() {
        return MapstructUtils.convert(mapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeChunkDO::getDelFlag, 0)), KnowledgeChunkBO.class);
    }

    public long count() {
        return mapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeChunkDO::getDelFlag, 0));
    }

    public List<KnowledgeChunkBO> findBySpace(Long spaceId) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeChunkDO::getSpaceId, spaceId)
                        .eq(KnowledgeChunkDO::getDelFlag, 0)
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
