package com.shiyu.ai.dal.repository.knowledge;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class KnowledgeDocRelationRepository {
    @Resource
    private KnowledgeDocRelationMapper mapper;

    public void insert(KnowledgeDocRelationDO r) { mapper.insert(r); }
    public void deleteByDocId(Long id) { mapper.deleteByQuery(QueryWrapper.create().eq("doc_id", id)); }
    public void deleteByKnowledgeId(Long id) { mapper.deleteByQuery(QueryWrapper.create().eq("knowledge_id", id)); }
    public List<KnowledgeDocRelationDO> selectByDocId(Long id) { return mapper.selectListByQuery(QueryWrapper.create().eq("doc_id", id)); }
    public List<KnowledgeDocRelationDO> selectByKnowledgeId(Long id) { return mapper.selectListByQuery(QueryWrapper.create().eq("knowledge_id", id)); }
    public void insertBatch(List<KnowledgeDocRelationDO> rels) { mapper.insertBatch(rels); }
}