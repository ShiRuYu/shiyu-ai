package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocumentMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;

@Component
public class KnowledgeDocumentRepository {

    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Resource
    private KnowledgeDocRelationMapper knowledgeDocRelationMapper;

    public KnowledgeDocumentBO selectById(Long id) {
        KnowledgeDocumentDO d = knowledgeDocumentMapper.selectOneById(id);
        return d != null ? MapstructUtils.convert(d, KnowledgeDocumentBO.class) : null;
    }

    public List<KnowledgeDocumentBO> selectAll() {
        return MapstructUtils.convert(knowledgeDocumentMapper.selectAll(), KnowledgeDocumentBO.class);
    }

    public int insert(KnowledgeDocumentBO bo) {
        KnowledgeDocumentDO dataObject = MapstructUtils.convert(bo, KnowledgeDocumentDO.class);
        int rows = knowledgeDocumentMapper.insert(dataObject);
        bo.setId(dataObject.getId());
        return rows;
    }

    public int update(KnowledgeDocumentBO bo) {
        return knowledgeDocumentMapper.update(MapstructUtils.convert(bo, KnowledgeDocumentDO.class));
    }

    public int deleteById(Long id) {
        return knowledgeDocumentMapper.deleteById(id);
    }

    public List<KnowledgeDocumentBO> searchByKeyword(String keyword, int topK) {
        return selectAll().stream()
                .filter(d -> d.getTitle() != null && d.getTitle().contains(keyword)
                        || d.getContent() != null && d.getContent().contains(keyword))
                .limit(topK)
                .toList();
    }

    public List<KnowledgeDocumentBO> selectByKnowledgeId(Long knowledgeId) {
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", knowledgeId))
                .stream().map(KnowledgeDocRelationDO::getDocId).toList();
        if (docIds.isEmpty()) return List.of();
        return MapstructUtils.convert(knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create().in("id", docIds)), KnowledgeDocumentBO.class);
    }

    public List<KnowledgeDocumentBO> selectByKnowledgeId(Long spaceId, Long knowledgeId) {
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(
                QueryWrapper.create().eq("space_id", spaceId).eq("knowledge_id", knowledgeId))
                .stream().map(KnowledgeDocRelationDO::getDocId).toList();
        if (docIds.isEmpty()) return List.of();
        return MapstructUtils.convert(knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                        .in(KnowledgeDocumentDO::getId, docIds)), KnowledgeDocumentBO.class);
    }

    public PageData<KnowledgeDocumentBO> pageBySpace(Long spaceId, int pageNum, int pageSize,
                                                     String keyword, String lifecycleStatus) {
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                .eq(KnowledgeDocumentDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeDocumentDO::getTitle, keyword);
        }
        if (lifecycleStatus != null && !lifecycleStatus.isBlank()) {
            query.eq(KnowledgeDocumentDO::getLifecycleStatus, lifecycleStatus);
        }
        var page = knowledgeDocumentMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeDocumentDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeDocumentBO.class),
                page.getTotalRow());
    }

    public KnowledgeDocumentBO findBySpaceAndChecksum(Long spaceId, String checksum) {
        KnowledgeDocumentDO dataObject = knowledgeDocumentMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocumentDO::getChecksum, checksum)
                        .eq(KnowledgeDocumentDO::getDelFlag, 0)
                        .limit(1));
        return dataObject == null ? null
                : MapstructUtils.convert(dataObject, KnowledgeDocumentBO.class);
    }

    public List<KnowledgeDocumentBO> findBySpace(Long spaceId) {
        return MapstructUtils.convert(knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocumentDO::getDelFlag, 0)
                        .orderBy(KnowledgeDocumentDO::getId, true)), KnowledgeDocumentBO.class);
    }

    public void assignDefaultSpace(Long spaceId) {
        List<KnowledgeDocumentDO> records = knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create().isNull(KnowledgeDocumentDO::getSpaceId));
        for (KnowledgeDocumentDO record : records) {
            record.setSpaceId(spaceId);
            if (record.getLifecycleStatus() == null) {
                record.setLifecycleStatus("PUBLISHED");
            }
            if (record.getParseStatus() == null) {
                record.setParseStatus("READY");
            }
            knowledgeDocumentMapper.update(record);
        }
    }
}
