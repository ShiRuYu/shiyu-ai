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
                QueryWrapper.create().eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceId(Long sourceId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceId(Long spaceId, Long sourceId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetId(Long targetId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetId(Long spaceId, Long targetId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceIdAndType(Long sourceId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getRelationType, type)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findBySourceIdAndType(Long spaceId, Long sourceId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getRelationType, type)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetIdAndType(Long targetId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getRelationType, type)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
    }

    public List<KnowledgeRelationBO> findByTargetIdAndType(Long spaceId, Long targetId, String type) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getRelationType, type)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)), KnowledgeRelationBO.class);
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

    public int deleteBySourceAndTargetAndType(Long spaceId, Long sourceId, Long targetId,
                                              String type) {
        return relationMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
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

    public int deleteBySourceIdOrTargetId(Long spaceId, Long knowledgeId) {
        int count = relationMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, knowledgeId));
        count += relationMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getTargetId, knowledgeId));
        return count;
    }

    public List<KnowledgeRelationBO> findBySpace(Long spaceId) {
        return MapstructUtils.convert(relationMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getDelFlag, 0)),
                KnowledgeRelationBO.class);
    }

    public boolean exists(Long spaceId, Long sourceId, Long targetId, String type) {
        return relationMapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)
                .eq(KnowledgeRelationDO::getRelationType, type)
                .eq(KnowledgeRelationDO::getDelFlag, 0)) > 0;
    }

    public void assignDefaultSpace(Long spaceId) {
        List<KnowledgeRelationDO> records = relationMapper.selectListByQuery(
                QueryWrapper.create().isNull(KnowledgeRelationDO::getSpaceId));
        for (KnowledgeRelationDO record : records) {
            record.setSpaceId(spaceId);
            relationMapper.update(record);
        }
    }
}
