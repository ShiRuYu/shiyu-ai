package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocRelationBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocRelationRepository {

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

    public List<KnowledgeDocRelationBO> selectByDocId(Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("doc_id", id)), KnowledgeDocRelationBO.class);
    }

    public List<KnowledgeDocRelationBO> selectByKnowledgeId(Long id) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", id)), KnowledgeDocRelationBO.class);
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
