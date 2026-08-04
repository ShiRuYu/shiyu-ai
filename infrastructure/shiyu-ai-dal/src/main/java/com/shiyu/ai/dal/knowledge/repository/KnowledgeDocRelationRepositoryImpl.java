package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocRelationRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository {

    @Resource
    private KnowledgeDocRelationMapper mapper;

    public void insert(KnowledgeDocRelationBO bo) {
        mapper.insert(MapstructUtils.convert(bo, KnowledgeDocRelationDO.class));
    }

    public void insertBatch(List<KnowledgeDocRelationBO> boList) {
        mapper.insertBatch(MapstructUtils.convert(boList, KnowledgeDocRelationDO.class));
    }

    public void deleteByDocId(Long id) {
        mapper.deleteByQuery(QueryWrapper.create().eq("doc_id", id));
    }

    public void deleteByKnowledgeId(Long id) {
        mapper.deleteByQuery(QueryWrapper.create().eq("knowledge_id", id));
    }

    public void deleteByKnowledgeId(Long spaceId, Long id) {
        mapper.deleteByQuery(QueryWrapper.create().eq("space_id", spaceId)
                .eq("knowledge_id", id));
    }

    public List<KnowledgeDocRelationBO> selectByDocId(Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("doc_id", id).eq("del_flag", 0)), KnowledgeDocRelationBO.class);
    }

    public List<KnowledgeDocRelationBO> selectByDocId(Long spaceId, Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("space_id", spaceId).eq("doc_id", id)
                        .eq("del_flag", 0)),
                KnowledgeDocRelationBO.class);
    }

    public List<KnowledgeDocRelationBO> selectByKnowledgeId(Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", id).eq("del_flag", 0)), KnowledgeDocRelationBO.class);
    }

    public List<KnowledgeDocRelationBO> selectByKnowledgeId(Long spaceId, Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("space_id", spaceId).eq("knowledge_id", id)
                        .eq("del_flag", 0)),
                KnowledgeDocRelationBO.class);
    }

    public void deleteByDocId(Long spaceId, Long id) {
        mapper.deleteByQuery(QueryWrapper.create().eq("space_id", spaceId)
                .eq("doc_id", id));
    }

    public void assignDefaultSpace(Long spaceId) {
        List<KnowledgeDocRelationDO> records = mapper.selectListByQuery(
                QueryWrapper.create().isNull(KnowledgeDocRelationDO::getSpaceId));
        for (KnowledgeDocRelationDO record : records) {
            record.setSpaceId(spaceId);
            mapper.update(record);
        }
    }
}
