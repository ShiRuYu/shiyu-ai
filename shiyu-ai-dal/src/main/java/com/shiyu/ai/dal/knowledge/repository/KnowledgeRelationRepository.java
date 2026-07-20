package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeRelationBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeRelationDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRelationRepository {

    @Resource
    private KnowledgeRelationMapper relationMapper;

    public List<KnowledgeRelationBO> findAll() {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create()), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceId(Long sourceId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSourceId, sourceId)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetId(Long targetId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getTargetId, targetId)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceIdAndType(Long sourceId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getRelationType, type)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetIdAndType(Long targetId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getRelationType, type)), KnowledgeRelationBO.class);
    }

    public int insert(KnowledgeRelationBO bo) {
        return relationMapper.insert(MapstructUtils.convert(bo, KnowledgeRelationDO.class));
    }

    public int deleteBySourceAndTargetAndType(Long sourceId, Long targetId, String type) {
        return relationMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)
                .eq(KnowledgeRelationDO::getRelationType, type));
    }

    public int deleteBySourceIdOrTargetId(Long knowledgeId) {
        int count = relationMapper.deleteByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSourceId, knowledgeId));
        count += relationMapper.deleteByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getTargetId, knowledgeId));
        return count;
    }
}
