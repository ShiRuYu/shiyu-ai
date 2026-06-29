package com.shiyu.ai.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeRelationDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRelationRepository {

    @Resource
    private KnowledgeRelationMapper relationMapper;

    public List<KnowledgeRelationDO> findAll() {
        return relationMapper.selectListByQuery(QueryWrapper.create());
    }

    public List<KnowledgeRelationDO> findBySourceId(Long sourceId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeRelationDO::getSourceId, sourceId);
        return relationMapper.selectListByQuery(qw);
    }

    public List<KnowledgeRelationDO> findByTargetId(Long targetId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeRelationDO::getTargetId, targetId);
        return relationMapper.selectListByQuery(qw);
    }

    public List<KnowledgeRelationDO> findBySourceIdAndType(Long sourceId, String type) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeRelationDO::getSourceId, sourceId);
        qw.eq(KnowledgeRelationDO::getRelationType, type);
        return relationMapper.selectListByQuery(qw);
    }

    public List<KnowledgeRelationDO> findByTargetIdAndType(Long targetId, String type) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeRelationDO::getTargetId, targetId);
        qw.eq(KnowledgeRelationDO::getRelationType, type);
        return relationMapper.selectListByQuery(qw);
    }

    public int insert(KnowledgeRelationDO relation) {
        return relationMapper.insert(relation);
    }

    public int deleteBySourceAndTargetAndType(Long sourceId, Long targetId, String type) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeRelationDO::getSourceId, sourceId);
        qw.eq(KnowledgeRelationDO::getTargetId, targetId);
        qw.eq(KnowledgeRelationDO::getRelationType, type);
        return relationMapper.deleteByQuery(qw);
    }
}
